package com.example.zorgmate;

import com.example.zorgmate.dal.entity.Invoice.Invoice;
import com.example.zorgmate.dal.entity.Invoice.InvoiceStatus;
import com.example.zorgmate.dal.repository.InvoiceRepository;
import com.example.zorgmate.dto.Invoice.CreateInvoiceRequestDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class InvoiceControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        Invoice invoice = Invoice.builder()
                .invoiceNumber("TEST-INV-001")
                .senderName("Sender")
                .receiverName("Receiver")
                .issueDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(14))
                .status(InvoiceStatus.UNPAID)
                .amount(new BigDecimal("100.00"))
                .createdBy("testuser")
                .build();

        invoiceRepository.save(invoice);
    }

    @Test
    @WithMockUser(username = "testuser")
    void getInvoiceById_shouldReturnOk() throws Exception {
        mockMvc.perform(get("/api/invoices/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.invoiceNumber").value("TEST-INV-001"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void deleteInvoice_shouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/invoices/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "testuser")
    void updateInvoiceStatus_shouldReturnOk() throws Exception {
        mockMvc.perform(patch("/api/invoices/1/status/PAID"))
                .andExpect(status().isOk())
                .andExpect(content().string("Status bijgewerkt naar PAID"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void getAllInvoices_shouldReturnList() throws Exception {
        mockMvc.perform(get("/api/invoices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].invoiceNumber").value("TEST-INV-001"));
    }

    @Test
    @WithMockUser(username = "andere_user")
    void getInvoiceById_shouldReturn403_whenFactuurIsNietVanGebruiker() throws Exception {
        mockMvc.perform(get("/api/invoices/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "andere_user")
    void deleteInvoice_shouldReturn403_whenFactuurNietVanGebruiker() throws Exception {
        mockMvc.perform(delete("/api/invoices/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "testuser")
    void updateInvoice_shouldReturn400_whenMissingFields() throws Exception {
        CreateInvoiceRequestDTO dto = new CreateInvoiceRequestDTO();

        mockMvc.perform(put("/api/invoices/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.invoiceNumber").exists());
    }

    @Test
    @WithMockUser(username = "testuser")
    void updateInvoiceStatus_shouldReturn404_whenNotFound() throws Exception {
        mockMvc.perform(patch("/api/invoices/999/status/PAID"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "nieuw_user_zonder_facturen")
    void getAllInvoices_shouldReturnEmptyList() throws Exception {
        mockMvc.perform(get("/api/invoices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }





}
