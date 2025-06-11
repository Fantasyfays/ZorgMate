package com.example.zorgmate;

import com.example.zorgmate.dal.entity.Client.Client;
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
import com.example.zorgmate.service.impl.InvoiceServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class InvoiceServiceImplTest {

    @Mock
    private InvoiceRepository invoiceRepository;
    @Mock
    private InvoiceItemRepository invoiceItemRepository;
    @Mock
    private TimeEntryRepository timeEntryRepository;

    @InjectMocks
    private InvoiceServiceImpl invoiceService;

    private Invoice invoice;
    private List<InvoiceItem> items;

    @BeforeEach
    void setUp() {
        invoice = Invoice.builder()
                .id(1L)
                .invoiceNumber("INV-2025-0001")
                .issueDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(14))
                .status(InvoiceStatus.UNPAID)
                .createdBy("testuser")
                .senderName("sender")
                .receiverName("receiver")
                .amount(BigDecimal.valueOf(200))
                .build();

        InvoiceItem item = InvoiceItem.builder()
                .id(1L)
                .description("Test Item")
                .hoursWorked(2)
                .hourlyRate(BigDecimal.valueOf(100))
                .subTotal(BigDecimal.valueOf(200))
                .invoice(invoice)
                .build();

        items = List.of(item);
    }

    @Test
    void getInvoicesForUser_shouldReturnInvoices() {
        when(invoiceRepository.findByCreatedBy("testuser")).thenReturn(List.of(invoice));
        when(invoiceItemRepository.findByInvoiceId(1L)).thenReturn(items);

        List<InvoiceResponseDTO> result = invoiceService.getInvoicesForUser("testuser");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getInvoiceNumber()).isEqualTo("INV-2025-0001");
    }

    @Test
    void getInvoiceByIdForUser_shouldReturnInvoice() {
        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));
        when(invoiceItemRepository.findByInvoiceId(1L)).thenReturn(items);

        InvoiceResponseDTO result = invoiceService.getInvoiceByIdForUser(1L, "testuser");

        assertThat(result).isNotNull();
        assertThat(result.getInvoiceNumber()).isEqualTo("INV-2025-0001");
    }

    @Test
    void getInvoiceByIdForUser_shouldThrow_whenNotFound() {
        when(invoiceRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> invoiceService.getInvoiceByIdForUser(1L, "testuser"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Factuur niet gevonden");
    }

    @Test
    void getInvoiceByIdForUser_shouldThrow_whenNotOwnedByUser() {
        invoice.setCreatedBy("otherUser");
        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));

        assertThatThrownBy(() -> invoiceService.getInvoiceByIdForUser(1L, "testuser"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Factuur niet gevonden");
    }

    @Test
    void updateInvoiceForUser_shouldUpdateInvoice() {
        CreateInvoiceRequestDTO dto = CreateInvoiceRequestDTO.builder()
                .invoiceNumber("INV-2025-0002")
                .senderName("sender")
                .receiverName("receiver")
                .issueDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(14))
                .status("UNPAID")
                .items(List.of(new InvoiceItemDTO("item", 2, BigDecimal.valueOf(100), BigDecimal.valueOf(200), LocalDate.now())))
                .build();

        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));
        when(invoiceItemRepository.findByInvoiceId(1L)).thenReturn(items);
        when(invoiceItemRepository.saveAll(anyList())).thenReturn(items);
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(invoice);

        InvoiceResponseDTO result = invoiceService.updateInvoiceForUser(1L, dto, "testuser");

        assertThat(result.getInvoiceNumber()).isEqualTo("INV-2025-0002");
        verify(invoiceItemRepository).deleteAll(anyList());
        verify(invoiceItemRepository).saveAll(anyList());
    }

    @Test
    void updateInvoiceForUser_shouldThrow_whenNotFound() {
        when(invoiceRepository.findById(1L)).thenReturn(Optional.empty());
        CreateInvoiceRequestDTO dto = CreateInvoiceRequestDTO.builder().build();

        assertThatThrownBy(() -> invoiceService.updateInvoiceForUser(1L, dto, "testuser"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Factuur niet gevonden");
    }

    @Test
    void autoGenerateInvoiceFromUnbilled_shouldGenerateInvoice() {
        TimeEntry entry = TimeEntry.builder()
                .id(1L)
                .hours(2)
                .hourlyRate(BigDecimal.valueOf(100))
                .description("Work")
                .client(Client.builder().name("receiver").build())
                .createdBy("testuser")
                .build();

        when(timeEntryRepository.findByClientIdAndInvoiceIsNull(1L)).thenReturn(List.of(entry));
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(invoice);
        when(invoiceItemRepository.saveAll(anyList())).thenReturn(items);
        when(timeEntryRepository.saveAll(anyList())).thenReturn(List.of(entry));

        InvoiceResponseDTO result = invoiceService.autoGenerateInvoiceFromUnbilled(1L, "testuser");

        assertThat(result).isNotNull();
        assertThat(result.getReceiverName()).isEqualTo("receiver");
        verify(invoiceRepository).save(any(Invoice.class));
        verify(invoiceItemRepository).saveAll(anyList());
        verify(timeEntryRepository).saveAll(anyList());
    }

    @Test
    void autoGenerateInvoiceFromUnbilled_shouldThrow_whenNoEntries() {
        when(timeEntryRepository.findByClientIdAndInvoiceIsNull(1L)).thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> invoiceService.autoGenerateInvoiceFromUnbilled(1L, "testuser"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Geen ongefactureerde uren gevonden.");
    }

    @Test
    void updateInvoiceStatusForUser_shouldUpdateStatus() {
        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));

        invoiceService.updateInvoiceStatusForUser(1L, InvoiceStatus.PAID, "testuser");

        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.PAID);
        verify(invoiceRepository).save(invoice);
    }

    @Test
    void updateInvoiceStatusForUser_shouldThrow_whenNotFound() {
        when(invoiceRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> invoiceService.updateInvoiceStatusForUser(1L, InvoiceStatus.PAID, "testuser"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Factuur niet gevonden");
    }

    @Test
    void deleteInvoiceForUser_shouldDeleteInvoice() {
        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));

        invoiceService.deleteInvoiceForUser(1L, "testuser");

        verify(invoiceRepository).delete(invoice);
    }

    @Test
    void deleteInvoiceForUser_shouldThrowForbidden_whenUserNotOwner() {
        invoice.setCreatedBy("otherUser");
        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));

        assertThatThrownBy(() -> invoiceService.deleteInvoiceForUser(1L, "testuser"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Je mag deze factuur niet verwijderen.");
    }

    @Test
    void deleteInvoiceForUser_shouldThrowNotFound_whenInvoiceNotFound() {
        when(invoiceRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> invoiceService.deleteInvoiceForUser(1L, "testuser"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Factuur niet gevonden");
    }
}
