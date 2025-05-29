package com.example.zorgmate;

import com.example.zorgmate.dal.entity.Client.Client;
import com.example.zorgmate.dal.entity.Invoice.*;
import com.example.zorgmate.dal.repository.InvoiceItemRepository;
import com.example.zorgmate.dal.repository.InvoiceRepository;
import com.example.zorgmate.dal.repository.TimeEntryRepository;
import com.example.zorgmate.dto.Invoice.CreateInvoiceRequestDTO;
import com.example.zorgmate.dto.Invoice.InvoiceItemDTO;
import com.example.zorgmate.dto.Invoice.InvoiceResponseDTO;
import com.example.zorgmate.service.impl.InvoiceServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.nio.file.AccessDeniedException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InvoiceServiceImplTest {

    @Mock private InvoiceRepository invoiceRepository;
    @Mock private InvoiceItemRepository invoiceItemRepository;
    @Mock private TimeEntryRepository timeEntryRepository;

    @InjectMocks private InvoiceServiceImpl invoiceService;

    private final String USERNAME = "user1";

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }
    @Test
    void deleteInvoiceForUser_shouldDeleteInvoiceItemsAndTimeEntries_whenOwnedByUser() {
        // Arrange
        Long invoiceId = 10L;
        String username = "user1";

        TimeEntry timeEntry = TimeEntry.builder()
                .id(100L)
                .description("Adviesgesprek")
                .createdBy(username)
                .build();

        InvoiceItem item = InvoiceItem.builder()
                .id(200L)
                .description("Consult")
                .timeEntry(timeEntry)
                .build();

        Invoice invoice = Invoice.builder()
                .id(invoiceId)
                .createdBy(username)
                .items(List.of(item))
                .timeEntries(List.of(timeEntry))
                .build();

        // Zet parent referenties
        item.setInvoice(invoice);
        timeEntry.setInvoice(invoice);

        // Mocks
        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));

        // Act
        invoiceService.deleteInvoiceForUser(invoiceId, username);

        // Assert
        verify(invoiceRepository).delete(invoice);
        verifyNoMoreInteractions(invoiceItemRepository, timeEntryRepository);
    }

    @Test
    void deleteInvoiceForUser_shouldThrowForbidden_whenUserIsNotOwner() {
        Long invoiceId = 11L;
        Invoice invoice = Invoice.builder()
                .id(invoiceId)
                .createdBy("andereGebruiker")
                .build();

        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                invoiceService.deleteInvoiceForUser(invoiceId, "user1")
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    }

    @Test
    void autoGenerateInvoiceFromUnbilled_shouldReturnInvoiceDTO_whenEntriesExist() {
        Client client = Client.builder().id(1L).name("Klant A").build();
        TimeEntry entry = TimeEntry.builder()
                .id(1L)
                .client(client)
                .description("Consult")
                .hours(2)
                .hourlyRate(BigDecimal.valueOf(50))
                .createdBy(USERNAME)
                .build();

        Invoice invoice = Invoice.builder()
                .id(100L)
                .invoiceNumber("INV-2025-0001")
                .receiverName("Klant A")
                .senderName("ZorgMate")
                .issueDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(14))
                .status(InvoiceStatus.UNPAID)
                .amount(BigDecimal.valueOf(100))
                .createdBy(USERNAME)
                .build();

        when(timeEntryRepository.findByClientIdAndInvoiceIsNull(1L)).thenReturn(List.of(entry));
        when(invoiceRepository.count()).thenReturn(0L);
        when(invoiceRepository.save(any())).thenReturn(invoice);

        InvoiceResponseDTO result = invoiceService.autoGenerateInvoiceFromUnbilled(1L, USERNAME);

        assertNotNull(result);
        assertEquals("Klant A", result.getReceiverName());
        assertEquals(BigDecimal.valueOf(100), result.getTotalAmount());
    }

    @Test
    void autoGenerateInvoiceFromUnbilled_shouldThrow_whenNoEntriesForUser() {
        TimeEntry otherUserEntry = TimeEntry.builder()
                .id(2L)
                .createdBy("someoneElse")
                .client(Client.builder().id(2L).build())
                .build();

        when(timeEntryRepository.findByClientIdAndInvoiceIsNull(2L)).thenReturn(List.of(otherUserEntry));

        assertThrows(ResponseStatusException.class, () ->
                invoiceService.autoGenerateInvoiceFromUnbilled(2L, USERNAME));
    }

    @Test
    void getInvoiceByIdForUser_shouldReturnInvoice_ifOwnedByUser() {
        Invoice invoice = Invoice.builder()
                .id(1L)
                .createdBy(USERNAME)
                .invoiceNumber("INV-2025-001")
                .status(InvoiceStatus.PAID)
                .build();

        InvoiceItem item = InvoiceItem.builder()
                .description("Test")
                .subTotal(BigDecimal.TEN)
                .build();

        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));
        when(invoiceItemRepository.findByInvoiceId(1L)).thenReturn(List.of(item));

        InvoiceResponseDTO result = invoiceService.getInvoiceByIdForUser(1L, USERNAME);

        assertEquals("INV-2025-001", result.getInvoiceNumber());
    }

    @Test
    void getInvoiceByIdForUser_shouldThrow_ifNotOwned() {
        Invoice invoice = Invoice.builder().id(2L).createdBy("notUser1").build();
        when(invoiceRepository.findById(2L)).thenReturn(Optional.of(invoice));

        assertThrows(ResponseStatusException.class, () ->
                invoiceService.getInvoiceByIdForUser(2L, USERNAME));
    }

    @Test
    void updateInvoiceStatusForUser_shouldSucceed_ifOwnedByUser() {
        Invoice invoice = Invoice.builder().id(3L).status(InvoiceStatus.UNPAID).createdBy(USERNAME).build();
        when(invoiceRepository.findById(3L)).thenReturn(Optional.of(invoice));

        invoiceService.updateInvoiceStatusForUser(3L, InvoiceStatus.PAID, USERNAME);

        verify(invoiceRepository).save(invoice);
        assertEquals(InvoiceStatus.PAID, invoice.getStatus());
    }

    @Test
    void deleteInvoiceForUser_shouldRemove_ifOwnedByUser() {
        Invoice invoice = Invoice.builder().id(4L).createdBy(USERNAME).build();
        when(invoiceRepository.findById(4L)).thenReturn(Optional.of(invoice));

        invoiceService.deleteInvoiceForUser(4L, USERNAME);

        verify(invoiceRepository).delete(invoice);
        verifyNoMoreInteractions(invoiceItemRepository, timeEntryRepository);
    }


    @Test
    void deleteInvoiceForUser_shouldThrow_ifNotOwned() {
        Invoice invoice = Invoice.builder().id(5L).createdBy("notUser1").build();
        when(invoiceRepository.findById(5L)).thenReturn(Optional.of(invoice));

        assertThrows(ResponseStatusException.class, () ->
                invoiceService.deleteInvoiceForUser(5L, USERNAME));
    }

}
