package com.example.zorgmate;

import com.example.zorgmate.dal.entity.Invoice.Invoice;
import com.example.zorgmate.dal.entity.Invoice.InvoiceStatus;
import com.example.zorgmate.dal.repository.InvoiceItemRepository;
import com.example.zorgmate.dal.repository.InvoiceRepository;
import com.example.zorgmate.dto.Invoice.CreateInvoiceRequestDTO;
import com.example.zorgmate.dto.Invoice.InvoiceResponseDTO;
import com.example.zorgmate.service.impl.InvoiceServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class InvoiceServiceImplTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private InvoiceItemRepository invoiceItemRepository;

    @InjectMocks
    private InvoiceServiceImpl invoiceService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    void testCreateInvoiceMetGeenItems() {
        CreateInvoiceRequestDTO dto = CreateInvoiceRequestDTO.builder()
                .invoiceNumber("INV-002")
                .senderName("ZorgMate")
                .receiverName("Klant B")
                .issueDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(10))
                .status("UNPAID")
                .items(List.of())
                .build();

        Invoice saved = Invoice.builder()
                .id(3L)
                .invoiceNumber("INV-002")
                .senderName("ZorgMate")
                .receiverName("Klant B")
                .issueDate(dto.getIssueDate())
                .dueDate(dto.getDueDate())
                .status(InvoiceStatus.UNPAID)
                .amount(BigDecimal.ZERO)
                .build();

        when(invoiceRepository.save(any())).thenReturn(saved);

        InvoiceResponseDTO response = invoiceService.createInvoice(dto);

        assertNotNull(response);
        assertEquals(BigDecimal.ZERO, response.getTotalAmount());
        assertEquals(0, response.getItems().size());
    }
}
