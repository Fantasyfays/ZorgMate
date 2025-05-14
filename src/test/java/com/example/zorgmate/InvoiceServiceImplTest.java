package com.example.zorgmate;

import com.example.zorgmate.dal.entity.Client.Client;
import com.example.zorgmate.dal.entity.Invoice.*;
import com.example.zorgmate.dal.repository.InvoiceItemRepository;
import com.example.zorgmate.dal.repository.InvoiceRepository;
import com.example.zorgmate.dal.repository.TimeEntryRepository;
import com.example.zorgmate.dto.Invoice.InvoiceResponseDTO;
import com.example.zorgmate.service.impl.InvoiceServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class InvoiceServiceImplTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private InvoiceItemRepository invoiceItemRepository;

    @Mock
    private TimeEntryRepository timeEntryRepository;

    @InjectMocks
    private InvoiceServiceImpl invoiceService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAutoGenerateInvoiceFromUnbilled() {
        Long clientId = 1L;

        Client dummyClient = Client.builder()
                .id(clientId)
                .name("Klant A")
                .build();

        TimeEntry timeEntry = TimeEntry.builder()
                .id(1L)
                .client(dummyClient)
                .description("Consult")
                .hours(2)
                .hourlyRate(BigDecimal.valueOf(50))
                .build();

        Invoice dummyInvoice = Invoice.builder()
                .id(100L)
                .invoiceNumber("INV-2025-0001")
                .receiverName("Klant A")
                .senderName("ZorgMate")
                .issueDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(14))
                .status(InvoiceStatus.UNPAID)
                .amount(BigDecimal.valueOf(100))
                .build();

        when(timeEntryRepository.findByClientIdAndInvoiceIsNull(clientId)).thenReturn(List.of(timeEntry));
        when(invoiceRepository.count()).thenReturn(0L);
        when(invoiceRepository.save(any())).thenReturn(dummyInvoice);
        when(invoiceItemRepository.saveAll(any())).thenReturn(null); // niet relevant voor de assert
        when(timeEntryRepository.saveAll(any())).thenReturn(null);

        InvoiceResponseDTO result = invoiceService.autoGenerateInvoiceFromUnbilled(clientId);

        assertNotNull(result);
        assertEquals("Klant A", result.getReceiverName());
        assertEquals("ZorgMate", result.getSenderName());
        assertEquals(BigDecimal.valueOf(100), result.getTotalAmount());
        assertEquals(1, result.getItems().size());
        assertEquals("Consult", result.getItems().get(0).getDescription());
    }
}
