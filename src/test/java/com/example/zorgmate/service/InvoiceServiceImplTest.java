package com.example.zorgmate.service;

import com.example.zorgmate.dal.entity.Client.Client;
import com.example.zorgmate.dal.entity.Invoice.*;
import com.example.zorgmate.dal.repository.InvoiceItemRepository;
import com.example.zorgmate.dal.repository.InvoiceRepository;
import com.example.zorgmate.dal.repository.TimeEntryRepository;
import com.example.zorgmate.dal.repository.UserRepository;
import com.example.zorgmate.dto.Invoice.CreateInvoiceRequestDTO;
import com.example.zorgmate.dto.Invoice.InvoiceItemDTO;
import com.example.zorgmate.dto.Invoice.InvoiceResponseDTO;
import com.example.zorgmate.exception.AccessDeniedToInvoiceException;
import com.example.zorgmate.exception.InvoiceNotFoundException;
import com.example.zorgmate.exception.NoUnbilledHoursFoundException;
import com.example.zorgmate.service.impl.InvoiceServiceImpl;
import com.example.zorgmate.websocket.InvoiceWebSocketHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
    private UserRepository userRepository;

    @BeforeEach
    public void setup() {
        invoiceRepository = mock(InvoiceRepository.class);
        invoiceItemRepository = mock(InvoiceItemRepository.class);
        timeEntryRepository = mock(TimeEntryRepository.class);
        webSocketHandler = mock(InvoiceWebSocketHandler.class);
        userRepository = mock(UserRepository.class);
        invoiceService = new InvoiceServiceImpl(invoiceRepository, invoiceItemRepository, timeEntryRepository, webSocketHandler, userRepository);
    }

    @Test
    public void testUpdateInvoice_HappyFlow() {
        Invoice invoice = new Invoice();
        invoice.setId(1L);
        invoice.setCreatedBy("user");
        invoice.setReceiverEmail("klant@example.com");

        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));
        when(invoiceItemRepository.findByInvoiceId(1L)).thenReturn(List.of());

        InvoiceItemDTO itemDTO = new InvoiceItemDTO("Werk", 2, new BigDecimal("50"), null, LocalDate.now());

        CreateInvoiceRequestDTO request = CreateInvoiceRequestDTO.builder()
                .invoiceNumber("INV-123")
                .senderName("Jan")
                .receiverName("Klant")
                .issueDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(7))
                .status("UNPAID")
                .items(List.of(itemDTO))
                .build();

        InvoiceResponseDTO response = invoiceService.updateInvoiceForUser(1L, request, "user");

        assertEquals("INV-123", response.getInvoiceNumber());
        assertEquals(new BigDecimal("100"), response.getTotalAmount());
        verify(invoiceRepository).save(invoice);
        verify(invoiceItemRepository).deleteAll(any());
        verify(invoiceItemRepository).saveAll(any());
        verify(webSocketHandler).sendToUser("user", "klant@example.com", "factuur_bijgewerkt1");
    }

    @Test
    public void testUpdateInvoice_WrongUser_ThrowsException() {
        Invoice invoice = new Invoice();
        invoice.setId(2L);
        invoice.setCreatedBy("creator");

        when(invoiceRepository.findById(2L)).thenReturn(Optional.of(invoice));

        CreateInvoiceRequestDTO dto = new CreateInvoiceRequestDTO();
        dto.setItems(List.of());

        assertThrows(AccessDeniedToInvoiceException.class, () ->
                invoiceService.updateInvoiceForUser(2L, dto, "intruder"));
    }

    @Test
    public void testUpdateInvoice_NotFound_ThrowsException() {
        when(invoiceRepository.findById(3L)).thenReturn(Optional.empty());

        CreateInvoiceRequestDTO dto = new CreateInvoiceRequestDTO();
        dto.setItems(List.of());

        assertThrows(InvoiceNotFoundException.class, () ->
                invoiceService.updateInvoiceForUser(3L, dto, "anyUser"));
    }

    @Test
    public void testAutoGenerateInvoiceFromUnbilled_HappyFlow() {
        TimeEntry entry = new TimeEntry();
        entry.setHours(3);
        entry.setHourlyRate(new BigDecimal("60"));
        entry.setDescription("Werk");
        entry.setCreatedBy("user");
        entry.setDate(LocalDate.now());

        Client client = new Client();
        client.setName("TestKlant");
        client.setEmail("klant@example.com");

        entry.setClient(client);

        when(timeEntryRepository.findByClientIdAndInvoiceIsNull(1L)).thenReturn(List.of(entry));
        when(invoiceRepository.count()).thenReturn(4L);
        when(invoiceRepository.save(any())).thenAnswer(inv -> {
            Invoice i = inv.getArgument(0);
            i.setId(123L);
            return i;
        });

        InvoiceResponseDTO result = invoiceService.autoGenerateInvoiceFromUnbilled(1L, "user");

        assertEquals(new BigDecimal("180"), result.getTotalAmount());
        assertEquals("TestKlant", result.getReceiverName());
        verify(invoiceRepository).save(any());
        verify(invoiceItemRepository).saveAll(any());
        verify(timeEntryRepository).saveAll(any());
        verify(webSocketHandler).sendToUser("user", "klant@example.com", "factuur_bijgewerkt123");
    }

    @Test
    public void testAutoGenerateInvoiceFromUnbilled_NoHours_ThrowsException() {
        TimeEntry entry = new TimeEntry();
        entry.setCreatedBy("otherUser");
        when(timeEntryRepository.findByClientIdAndInvoiceIsNull(10L)).thenReturn(List.of(entry));

        assertThrows(NoUnbilledHoursFoundException.class, () ->
                invoiceService.autoGenerateInvoiceFromUnbilled(10L, "user"));
    }
}
