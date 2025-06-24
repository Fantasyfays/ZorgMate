package com.example.zorgmate.service;

import com.example.zorgmate.dal.entity.Client.Client;
import com.example.zorgmate.dal.entity.Invoice.*;
import com.example.zorgmate.dal.repository.*;
import com.example.zorgmate.dto.Invoice.*;
import com.example.zorgmate.service.impl.InvoiceServiceImpl;
import com.example.zorgmate.websocket.InvoiceWebSocketHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class InvoiceServiceImplTest {

    private InvoiceRepository invoiceRepository;
    private InvoiceItemRepository invoiceItemRepository;
    private TimeEntryRepository timeEntryRepository;
    private InvoiceWebSocketHandler webSocketHandler;
    private InvoiceServiceImpl invoiceService;

    @BeforeEach
    public void setup() {
        invoiceRepository = mock(InvoiceRepository.class);
        invoiceItemRepository = mock(InvoiceItemRepository.class);
        timeEntryRepository = mock(TimeEntryRepository.class);
        webSocketHandler = mock(InvoiceWebSocketHandler.class);
        invoiceService = new InvoiceServiceImpl(invoiceRepository, invoiceItemRepository, timeEntryRepository, webSocketHandler);
    }

    @Test
    public void testUpdateInvoice_HappyFlow() {
        Invoice existingInvoice = new Invoice();
        existingInvoice.setId(1L);
        existingInvoice.setCreatedBy("user");

        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(existingInvoice));
        when(invoiceItemRepository.findByInvoiceId(1L)).thenReturn(Collections.emptyList());

        InvoiceItemDTO itemDTO = new InvoiceItemDTO("Consult", 2, new BigDecimal("50"), null, null);
        CreateInvoiceRequestDTO request = new CreateInvoiceRequestDTO();
        request.setInvoiceNumber("INV-001");
        request.setSenderName("Alice");
        request.setReceiverName("Bob");
        request.setIssueDate(LocalDate.now());
        request.setDueDate(LocalDate.now().plusDays(10));
        request.setStatus("UNPAID");
        request.setItems(List.of(itemDTO));

        InvoiceResponseDTO response = invoiceService.updateInvoiceForUser(1L, request, "user");

        assertEquals("INV-001", response.getInvoiceNumber());
        assertEquals(new BigDecimal("100"), response.getTotalAmount());
        verify(invoiceRepository).save(existingInvoice);
        verify(invoiceItemRepository).saveAll(anyList());
        verify(webSocketHandler).broadcastUpdate("factuur_bijgewerkt:1");
    }


    @Test
    public void testUpdateInvoice_ZeroHours_ShouldReturnZeroSubtotal() {
        Invoice invoice = new Invoice();
        invoice.setId(1L);
        invoice.setCreatedBy("user");

        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));
        when(invoiceItemRepository.findByInvoiceId(1L)).thenReturn(Collections.emptyList());

        InvoiceItemDTO item = new InvoiceItemDTO("Consult", 0, new BigDecimal("80"), null, null);
        CreateInvoiceRequestDTO dto = new CreateInvoiceRequestDTO();
        dto.setInvoiceNumber("INV-002");
        dto.setSenderName("Max");
        dto.setReceiverName("Lisa");
        dto.setIssueDate(LocalDate.now());
        dto.setDueDate(LocalDate.now().plusDays(10));
        dto.setStatus("UNPAID");
        dto.setItems(List.of(item));

        InvoiceResponseDTO response = invoiceService.updateInvoiceForUser(1L, dto, "user");

        assertEquals(BigDecimal.ZERO, response.getTotalAmount());
    }

    @Test
    public void testUpdateInvoice_NameTooLong_ShouldTrimOrFail() {
        String longName = "A".repeat(60);
        Invoice invoice = new Invoice();
        invoice.setId(1L);
        invoice.setCreatedBy("user");

        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));
        when(invoiceItemRepository.findByInvoiceId(1L)).thenReturn(Collections.emptyList());

        CreateInvoiceRequestDTO dto = new CreateInvoiceRequestDTO();
        dto.setInvoiceNumber("INV-003");
        dto.setSenderName(longName);
        dto.setReceiverName(longName);
        dto.setIssueDate(LocalDate.now());
        dto.setDueDate(LocalDate.now().plusDays(10));
        dto.setStatus("UNPAID");
        dto.setItems(Collections.emptyList());

        InvoiceResponseDTO response = invoiceService.updateInvoiceForUser(1L, dto, "user");

        assertTrue(response.getSenderName().length() > 50);
    }

    @Test
    public void testAutoGenerateInvoiceFromUnbilled_HappyFlow() {
        TimeEntry entry = new TimeEntry();
        entry.setHours(2);
        entry.setHourlyRate(new BigDecimal("60"));
        entry.setCreatedBy("user");
        Client client = new Client();
        client.setName("Klant A");
        entry.setClient(client);

        when(timeEntryRepository.findByClientIdAndInvoiceIsNull(1L)).thenReturn(List.of(entry));
        when(invoiceRepository.save(any())).thenAnswer(i -> {
            Invoice inv = i.getArgument(0);
            inv.setId(1L);
            return inv;
        });

        InvoiceResponseDTO response = invoiceService.autoGenerateInvoiceFromUnbilled(1L, "user");

        assertEquals(new BigDecimal("120"), response.getTotalAmount());
        verify(timeEntryRepository).saveAll(anyList());
    }


}
