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
import com.example.zorgmate.exception.AccessDeniedToInvoiceException;
import com.example.zorgmate.exception.InvoiceNotFoundException;
import com.example.zorgmate.exception.NoUnbilledHoursFoundException;
import com.example.zorgmate.service.interfaces.InvoiceService;
import com.example.zorgmate.websocket.InvoiceWebSocketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InvoiceServiceImpl implements InvoiceService {

    private static final Logger logger = LoggerFactory.getLogger(InvoiceServiceImpl.class);

    private final InvoiceRepository invoiceRepository;
    private final InvoiceItemRepository invoiceItemRepository;
    private final TimeEntryRepository timeEntryRepository;
    private final InvoiceWebSocketHandler webSocketHandler;

    public InvoiceServiceImpl(InvoiceRepository invoiceRepository,
                              InvoiceItemRepository invoiceItemRepository,
                              TimeEntryRepository timeEntryRepository,
                              InvoiceWebSocketHandler webSocketHandler) {
        this.invoiceRepository = invoiceRepository;
        this.invoiceItemRepository = invoiceItemRepository;
        this.timeEntryRepository = timeEntryRepository;
        this.webSocketHandler = webSocketHandler;
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
                .orElseThrow(() -> new InvoiceNotFoundException(id));

        if (!invoice.getCreatedBy().equals(username)) {
            throw new AccessDeniedToInvoiceException(id);
        }

        List<InvoiceItem> items = invoiceItemRepository.findByInvoiceId(invoice.getId());
        return mapToDTO(invoice, items);
    }

    @Override
    public InvoiceResponseDTO updateInvoiceForUser(Long id, CreateInvoiceRequestDTO dto, String username) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new InvoiceNotFoundException(id));

        if (!invoice.getCreatedBy().equals(username)) {
            throw new AccessDeniedToInvoiceException(id);
        }

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

        try {
            webSocketHandler.sendToUser(username,"factuur_bijgewerkt" + invoice.getId());
        } catch (Exception e) {
            logger.error("WebSocket broadcast failed for updated invoice {}", invoice.getId(), e);
        }

        return mapToDTO(invoice, updatedItems);
    }

    @Override
    public void deleteInvoiceForUser(Long invoiceId, String username) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new InvoiceNotFoundException(invoiceId));

        if (!invoice.getCreatedBy().equals(username)) {
            throw new AccessDeniedToInvoiceException(invoiceId);
        }

        invoiceRepository.delete(invoice);

        try {
            webSocketHandler.sendToUser(username,"factuur_bijgewerkt" + invoice.getId());
        } catch (Exception e) {
            logger.error("WebSocket broadcast failed for deleted invoice {}", invoiceId, e);
        }
    }

    @Override
    public void updateInvoiceStatusForUser(Long id, InvoiceStatus status, String username) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new InvoiceNotFoundException(id));

        if (!invoice.getCreatedBy().equals(username)) {
            throw new AccessDeniedToInvoiceException(id);
        }

        invoice.setStatus(status);
        invoiceRepository.save(invoice);
    }

    @Override
    public InvoiceResponseDTO autoGenerateInvoiceFromUnbilled(Long clientId, String username) {
        List<TimeEntry> entries = timeEntryRepository.findByClientIdAndInvoiceIsNull(clientId).stream()
                .filter(e -> e.getCreatedBy().equals(username))
                .collect(Collectors.toList());

        if (entries.isEmpty()) {
            throw new NoUnbilledHoursFoundException(clientId);
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

        try {
            webSocketHandler.sendToUser(username,"factuur_bijgewerkt" + invoice.getId());
        } catch (Exception e) {
            logger.error("WebSocket broadcast failed for auto-generated invoice {}", invoice.getId(), e);
        }

        return mapToDTO(invoice, items);
    }

    // Helpers

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
