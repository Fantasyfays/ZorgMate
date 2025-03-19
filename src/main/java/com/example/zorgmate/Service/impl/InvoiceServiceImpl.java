package com.example.zorgmate.Service.impl;

import com.example.zorgmate.Service.interfaces.InvoiceService;
import com.example.zorgmate.dal.entity.Invoice;
import com.example.zorgmate.dal.entity.InvoiceItem;
import com.example.zorgmate.dal.entity.InvoiceStatus;
import com.example.zorgmate.dal.repository.InvoiceItemRepository;
import com.example.zorgmate.dal.repository.InvoiceRepository;
import com.example.zorgmate.dto.Invoice.CreateInvoiceRequestDTO;
import com.example.zorgmate.dto.Invoice.InvoiceItemDTO;
import com.example.zorgmate.dto.Invoice.InvoiceResponseDTO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
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
    public InvoiceResponseDTO createInvoice(CreateInvoiceRequestDTO dto) {
        Invoice invoice = Invoice.builder()
                .invoiceNumber(dto.getInvoiceNumber())
                .senderName(dto.getSenderName())  // ✅ Zet de naam van de verzender
                .receiverName(dto.getReceiverName())  // ✅ Zet de naam van de ontvanger
                .amount(BigDecimal.ZERO)
                .issueDate(dto.getIssueDate())
                .dueDate(dto.getDueDate())
                .status(InvoiceStatus.valueOf(dto.getStatus().toUpperCase()))
                .build();

        Invoice savedInvoice = invoiceRepository.save(invoice);

        List<InvoiceItem> items = dto.getItems().stream()
                .map(itemDTO -> InvoiceItem.builder()
                        .description(itemDTO.getDescription())
                        .hoursWorked(itemDTO.getHoursWorked())
                        .hourlyRate(itemDTO.getHourlyRate())
                        .subTotal(itemDTO.getHourlyRate().multiply(BigDecimal.valueOf(itemDTO.getHoursWorked())))
                        .invoice(savedInvoice)
                        .build())
                .collect(Collectors.toList());

        invoiceItemRepository.saveAll(items);

        BigDecimal totalAmount = items.stream()
                .map(InvoiceItem::getSubTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        savedInvoice.setAmount(totalAmount);
        invoiceRepository.save(savedInvoice);

        List<InvoiceItemDTO> itemDTOs = items.stream()
                .map(item -> new InvoiceItemDTO(
                        item.getDescription(),
                        item.getHoursWorked(),
                        item.getHourlyRate(),
                        item.getSubTotal()))
                .collect(Collectors.toList());

        return InvoiceResponseDTO.builder()
                .id(savedInvoice.getId())
                .invoiceNumber(Integer.parseInt(savedInvoice.getInvoiceNumber()))
                .issueDate(savedInvoice.getIssueDate())
                .dueDate(savedInvoice.getDueDate())
                .status(savedInvoice.getStatus().name())
                .totalAmount(savedInvoice.getAmount())
                .senderName(savedInvoice.getSenderName())  // ✅ Stuur de naam van de verzender terug
                .receiverName(savedInvoice.getReceiverName())  // ✅ Stuur de naam van de ontvanger terug
                .items(itemDTOs)
                .build();
    }


    @Override
    public List<InvoiceResponseDTO> getAllInvoices() {
        return invoiceRepository.findAll().stream().map(invoice -> {
            List<InvoiceItemDTO> itemDTOs = invoiceItemRepository.findByInvoiceId(invoice.getId())
                    .stream()
                    .map(item -> new InvoiceItemDTO(
                            item.getDescription(),
                            item.getHoursWorked(),
                            item.getHourlyRate(),
                            item.getSubTotal()))
                    .collect(Collectors.toList());

            return InvoiceResponseDTO.builder()
                    .id(invoice.getId())
                    .invoiceNumber(Integer.parseInt(invoice.getInvoiceNumber()))
                    .issueDate(invoice.getIssueDate())
                    .dueDate(invoice.getDueDate())
                    .status(invoice.getStatus().name())
                    .totalAmount(invoice.getAmount())
                    .items(itemDTOs)
                    .build();
        }).collect(Collectors.toList());
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

        invoiceItemRepository.deleteAll(invoiceItemRepository.findByInvoiceId(id)); // ✅ Oude items verwijderen

        List<InvoiceItem> updatedItems = dto.getItems().stream()
                .map(itemDTO -> InvoiceItem.builder()
                        .description(itemDTO.getDescription())
                        .hoursWorked(itemDTO.getHoursWorked())
                        .hourlyRate(itemDTO.getHourlyRate())
                        .subTotal(itemDTO.getHourlyRate().multiply(BigDecimal.valueOf(itemDTO.getHoursWorked())))
                        .invoice(invoice)
                        .build())
                .collect(Collectors.toList());

        invoiceItemRepository.saveAll(updatedItems);  // ✅ Nieuwe items opslaan

        BigDecimal totalAmount = updatedItems.stream()
                .map(InvoiceItem::getSubTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        invoice.setAmount(totalAmount);
        invoiceRepository.save(invoice);

        List<InvoiceItemDTO> itemDTOs = updatedItems.stream()
                .map(item -> new InvoiceItemDTO(
                        item.getDescription(),
                        item.getHoursWorked(),
                        item.getHourlyRate(),
                        item.getSubTotal()))
                .collect(Collectors.toList());

        return InvoiceResponseDTO.builder()
                .id(invoice.getId())
                .invoiceNumber(Integer.parseInt(invoice.getInvoiceNumber()))
                .issueDate(invoice.getIssueDate())
                .dueDate(invoice.getDueDate())
                .status(invoice.getStatus().name())
                .totalAmount(invoice.getAmount())
                .senderName(invoice.getSenderName())
                .receiverName(invoice.getReceiverName())
                .items(itemDTOs)
                .build();
    }

    @Override
    public void deleteInvoice(Long id) {
        if (!invoiceRepository.existsById(id)) {
            throw new IllegalArgumentException("Factuur met ID " + id + " niet gevonden");
        }
        invoiceItemRepository.deleteAll(invoiceItemRepository.findByInvoiceId(id));  // ✅ Eerst de factuurregels verwijderen
        invoiceRepository.deleteById(id);  // ✅ Dan de factuur verwijderen
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
                .map(invoice -> InvoiceResponseDTO.builder()
                        .id(invoice.getId())
                        .invoiceNumber(Integer.parseInt(invoice.getInvoiceNumber()))
                        .issueDate(invoice.getIssueDate())
                        .dueDate(invoice.getDueDate())
                        .status(invoice.getStatus().name())
                        .totalAmount(invoice.getAmount())
                        .items(invoiceItemRepository.findByInvoiceId(invoice.getId())
                                .stream()
                                .map(item -> new InvoiceItemDTO(
                                        item.getDescription(),
                                        item.getHoursWorked(),
                                        item.getHourlyRate(),
                                        item.getSubTotal()))
                                .collect(Collectors.toList()))
                        .build())
                .collect(Collectors.toList());
    }
}
