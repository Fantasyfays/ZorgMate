package com.example.zorgmate.service.impl;

import com.example.zorgmate.service.interfaces.InvoiceService;
import com.example.zorgmate.dal.entity.Invoice.Invoice;
import com.example.zorgmate.dal.entity.Invoice.InvoiceItem;
import com.example.zorgmate.dal.entity.Invoice.InvoiceStatus;
import com.example.zorgmate.dal.repository.InvoiceItemRepository;
import com.example.zorgmate.dal.repository.InvoiceRepository;
import com.example.zorgmate.dto.Invoice.CreateInvoiceRequestDTO;
import com.example.zorgmate.dto.Invoice.InvoiceItemDTO;
import com.example.zorgmate.dto.Invoice.InvoiceResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceItemRepository invoiceItemRepository;

    public InvoiceServiceImpl(InvoiceRepository invoiceRepository, InvoiceItemRepository invoiceItemRepository) {
        this.invoiceRepository = invoiceRepository;
        this.invoiceItemRepository = invoiceItemRepository;
    }

    @Override
    public InvoiceResponseDTO getInvoiceById(Long id) {
        Optional<Invoice> invoiceOptional = invoiceRepository.findById(id);
        if (!invoiceOptional.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Factuur niet gevonden");
        }

        Invoice invoice = invoiceOptional.get();
        List<InvoiceItem> items = invoiceItemRepository.findByInvoiceId(invoice.getId());
        return mapToDTO(invoice, items);
    }

    @Override
    public InvoiceResponseDTO createInvoice(CreateInvoiceRequestDTO dto) {
        Invoice invoice = Invoice.builder()
                .invoiceNumber(dto.getInvoiceNumber())
                .senderName(dto.getSenderName())
                .receiverName(dto.getReceiverName())
                .amount(BigDecimal.ZERO)
                .issueDate(dto.getIssueDate())
                .dueDate(dto.getDueDate())
                .status(InvoiceStatus.valueOf(dto.getStatus().toUpperCase()))
                .build();

        Invoice savedInvoice = invoiceRepository.save(invoice);

        List<InvoiceItem> items = mapToEntities(dto.getItems(), savedInvoice);
        invoiceItemRepository.saveAll(items);

        BigDecimal totalAmount = calculateTotalAmount(items);
        savedInvoice.setAmount(totalAmount);
        invoiceRepository.save(savedInvoice);

        return mapToDTO(savedInvoice, items);
    }

    @Override
    public List<InvoiceResponseDTO> getAllInvoices() {
        return invoiceRepository.findAll().stream()
                .map(invoice -> {
                    List<InvoiceItem> items = invoiceItemRepository.findByInvoiceId(invoice.getId());
                    return mapToDTO(invoice, items);
                })
                .collect(Collectors.toList());
    }

    @Override
    public InvoiceResponseDTO updateInvoice(Long id, CreateInvoiceRequestDTO dto) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Factuur niet gevonden"));

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
    public void deleteInvoice(Long id) {
        if (!invoiceRepository.existsById(id)) {
            throw new IllegalArgumentException("Factuur met ID " + id + " niet gevonden");
        }
        invoiceItemRepository.deleteAll(invoiceItemRepository.findByInvoiceId(id));
        invoiceRepository.deleteById(id);
    }

    @Override
    public void updateInvoiceStatus(Long id, InvoiceStatus status) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Factuur niet gevonden"));

        invoice.setStatus(status);
        invoiceRepository.save(invoice);
    }

    @Override
    public List<InvoiceResponseDTO> getInvoicesByStatus(InvoiceStatus status) {
        return invoiceRepository.findByStatus(status).stream()
                .map(invoice -> {
                    List<InvoiceItem> items = invoiceItemRepository.findByInvoiceId(invoice.getId());
                    return mapToDTO(invoice, items);
                })
                .collect(Collectors.toList());
    }

    private InvoiceResponseDTO mapToDTO(Invoice invoice, List<InvoiceItem> items) {
        List<InvoiceItemDTO> itemDTOs = items.stream()
                .map(item -> new InvoiceItemDTO(
                        item.getDescription(),
                        item.getHoursWorked(),
                        item.getHourlyRate(),
                        item.getSubTotal()))
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
}
