package com.example.zorgmate.service.impl;

import com.example.zorgmate.dal.entity.Invoice.Invoice;
import com.example.zorgmate.dal.entity.Invoice.InvoiceItem;
import com.example.zorgmate.dal.entity.Invoice.InvoiceStatus;
import com.example.zorgmate.dal.entity.Invoice.TimeEntry;
import com.example.zorgmate.dal.repository.InvoiceItemRepository;
import com.example.zorgmate.dal.repository.InvoiceRepository;
import com.example.zorgmate.dal.repository.TimeEntryRepository;
import com.example.zorgmate.dto.Invoice.CreateInvoiceRequestDTO;
import com.example.zorgmate.dto.Invoice.InvoiceItemDTO;
import com.example.zorgmate.dto.Invoice.InvoiceResponseDTO;
import com.example.zorgmate.service.interfaces.InvoiceService;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import org.springframework.security.access.AccessDeniedException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceItemRepository invoiceItemRepository;
    private final TimeEntryRepository timeEntryRepository;

    public InvoiceServiceImpl(InvoiceRepository invoiceRepository,
                              InvoiceItemRepository invoiceItemRepository,
                              TimeEntryRepository timeEntryRepository) {
        this.invoiceRepository = invoiceRepository;
        this.invoiceItemRepository = invoiceItemRepository;
        this.timeEntryRepository = timeEntryRepository;
    }

    @Override
    public List<InvoiceResponseDTO> getInvoicesForUser(String username) {
        return invoiceRepository.findByCreatedBy(username).stream()
                .map(invoice -> {
                    List<InvoiceItem> items = invoiceItemRepository.findByInvoiceId(invoice.getId());
                    return mapToDTO(invoice, items);
                })
                .collect(Collectors.toList());
    }

    @Override
    public InvoiceResponseDTO getInvoiceByIdForUser(Long id, String username) {
        Invoice invoice = invoiceRepository.findById(id)
                .filter(i -> i.getCreatedBy().equals(username))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Factuur niet gevonden"));

        List<InvoiceItem> items = invoiceItemRepository.findByInvoiceId(invoice.getId());
        return mapToDTO(invoice, items);
    }

    @Override
    public InvoiceResponseDTO updateInvoiceForUser(Long id, CreateInvoiceRequestDTO dto, String username) {
        Invoice invoice = invoiceRepository.findById(id)
                .filter(i -> i.getCreatedBy().equals(username))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Factuur niet gevonden"));

        invoice.setInvoiceNumber(dto.getInvoiceNumber());
        invoice.setSenderName(dto.getSenderName());
        invoice.setReceiverName(dto.getReceiverName());
        invoice.setIssueDate(dto.getIssueDate());
        invoice.setDueDate(dto.getDueDate());
        invoice.setStatus(InvoiceStatus.valueOf(dto.getStatus().toUpperCase()));

        invoiceItemRepository.deleteAll(invoiceItemRepository.findByInvoiceId(id));

        List<InvoiceItem> updatedItems = mapToEntities(dto.getItems(), invoice);
        invoiceItemRepository.saveAll(updatedItems);

        BigDecimal totalAmount = calculateTotalAmount(updatedItems);
        invoice.setAmount(totalAmount);
        invoiceRepository.save(invoice);

        return mapToDTO(invoice, updatedItems);
    }

    @Override
    public void deleteInvoiceForUser(Long id, String username) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invoice not found"));

        if (!invoice.getCreatedBy().trim().equalsIgnoreCase(username.trim())) {
            throw new AccessDeniedException("Je mag deze factuur niet verwijderen.");
        }

        // 🔁 1. Eerst alle invoice items ophalen
        List<InvoiceItem> items = invoiceItemRepository.findByInvoiceId(invoice.getId());

        // 🔧 2. Ontkoppel alle gekoppelde timeEntries (anders krijg je constraint errors)
        for (InvoiceItem item : items) {
            item.setTimeEntry(null);
        }
        invoiceItemRepository.saveAll(items);

        // ✅ 3. Verwijder de invoice items
        invoiceItemRepository.deleteAll(items);

        // ✅ 4. Verwijder alle gekoppelde timeEntries
        List<TimeEntry> linkedEntries = timeEntryRepository.findByInvoiceId(invoice.getId());
        timeEntryRepository.deleteAll(linkedEntries);

        // ✅ 5. Verwijder tot slot de factuur
        invoiceRepository.delete(invoice);
    }




    @Override
    public void updateInvoiceStatusForUser(Long id, InvoiceStatus status, String username) {
        Invoice invoice = invoiceRepository.findById(id)
                .filter(i -> i.getCreatedBy().equals(username))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Factuur niet gevonden"));
        invoice.setStatus(status);
        invoiceRepository.save(invoice);
    }

    @Override
    public InvoiceResponseDTO autoGenerateInvoiceFromUnbilled(Long clientId, String username) {
        List<TimeEntry> entries = timeEntryRepository.findByClientIdAndInvoiceIsNull(clientId).stream()
                .filter(e -> e.getCreatedBy().equals(username))
                .collect(Collectors.toList());

        if (entries.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Geen ongefactureerde uren gevonden.");
        }

        List<InvoiceItem> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (TimeEntry entry : entries) {
            BigDecimal subtotal = entry.getHourlyRate().multiply(BigDecimal.valueOf(entry.getHours()));
            total = total.add(subtotal);

            InvoiceItem item = InvoiceItem.builder()
                    .description(entry.getDescription())
                    .hoursWorked(entry.getHours())
                    .hourlyRate(entry.getHourlyRate())
                    .subTotal(subtotal)
                    .timeEntry(entry)
                    .build();

            items.add(item);
        }

        Invoice invoice = Invoice.builder()
                .invoiceNumber(generateInvoiceNumber())
                .senderName(username)
                .receiverName(entries.get(0).getClient().getName())
                .amount(total)
                .issueDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(14))
                .status(InvoiceStatus.UNPAID)
                .createdBy(username)
                .build();

        invoice = invoiceRepository.save(invoice);

        for (InvoiceItem item : items) item.setInvoice(invoice);
        invoiceItemRepository.saveAll(items);

        for (TimeEntry entry : entries) entry.setInvoice(invoice);
        timeEntryRepository.saveAll(entries);

        return mapToDTO(invoice, items);
    }
    private InvoiceResponseDTO mapToDTO(Invoice invoice, List<InvoiceItem> items) {
        List<InvoiceItemDTO> itemDTOs = items.stream()
                .map(item -> new InvoiceItemDTO(
                        item.getDescription(),
                        item.getHoursWorked(),
                        item.getHourlyRate(),
                        item.getSubTotal(),
                        item.getTimeEntry() != null ? item.getTimeEntry().getDate() : null
                ))
                .collect(Collectors.toList());

        return InvoiceResponseDTO.builder()
                .id(invoice.getId())
                .invoiceNumber(invoice.getInvoiceNumber())
                .issueDate(invoice.getIssueDate())
                .dueDate(invoice.getDueDate())
                .status(invoice.getStatus().name())
                .totalAmount(invoice.getAmount())
                .senderName(invoice.getSenderName())
                .receiverName(invoice.getReceiverName())
                .items(itemDTOs)
                .build();
    }

    private List<InvoiceItem> mapToEntities(List<InvoiceItemDTO> itemDTOs, Invoice invoice) {
        return itemDTOs.stream()
                .map(itemDTO -> InvoiceItem.builder()
                        .description(itemDTO.getDescription())
                        .hoursWorked(itemDTO.getHoursWorked())
                        .hourlyRate(itemDTO.getHourlyRate())
                        .subTotal(itemDTO.getHourlyRate().multiply(BigDecimal.valueOf(itemDTO.getHoursWorked())))
                        .invoice(invoice)
                        .build())
                .collect(Collectors.toList());
    }

    private BigDecimal calculateTotalAmount(List<InvoiceItem> items) {
        return items.stream()
                .map(InvoiceItem::getSubTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String generateInvoiceNumber() {
        long count = invoiceRepository.count() + 1;
        return "INV-" + LocalDate.now().getYear() + "-" + String.format("%04d", count);
    }

}