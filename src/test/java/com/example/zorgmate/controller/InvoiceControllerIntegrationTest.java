package com.example.zorgmate.controller;

import com.example.zorgmate.dal.entity.Client.Client;
import com.example.zorgmate.dal.entity.Invoice.Invoice;
import com.example.zorgmate.dal.entity.Invoice.InvoiceStatus;
import com.example.zorgmate.dal.entity.Invoice.TimeEntry;
import com.example.zorgmate.dal.repository.ClientRepository;
import com.example.zorgmate.dal.repository.InvoiceRepository;
import com.example.zorgmate.dal.repository.TimeEntryRepository;
import com.example.zorgmate.dto.Invoice.AutoInvoiceRequestDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class InvoiceControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private TimeEntryRepository timeEntryRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "user1")
    public void testAutoGenerateInvoice_withHours_returns200AndCorrectAmount() throws Exception {
        Client klant = new Client();
        klant.setName("fays");
        klant.setEmail("fays@mail.com");
        klant.setPhone("0612345678");
        klant.setPostcode("1234AB");
        klant.setHouseNumber("12");
        klant.setStreet("Hoofdstraat");
        klant.setCity("Amsterdam");
        klant.setCreatedBy("user1");
        clientRepository.save(klant);

        TimeEntry uren = new TimeEntry();
        uren.setClient(klant);
        uren.setHours(5);
        uren.setHourlyRate(BigDecimal.valueOf(50));
        uren.setDescription("Werk");
        uren.setCreatedBy("user1");
        uren.setDate(LocalDate.now());
        timeEntryRepository.save(uren);

        AutoInvoiceRequestDTO dto = new AutoInvoiceRequestDTO();
        dto.setClientId(klant.getId());

        mockMvc.perform(post("/api/invoices/auto-generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.receiverName").value("fays"))
                .andExpect(jsonPath("$.totalAmount").value(250));
    }

    @Test
    @WithMockUser(username = "testuser")
    public void testAutoGenerateInvoice_withoutHours_returns400() throws Exception {
        Client klant = new Client();
        klant.setName("Klant");
        klant.setEmail("leeg@mail.com");
        klant.setPhone("0600000000");
        klant.setPostcode("0000AA");
        klant.setHouseNumber("1");
        klant.setStreet("LegeStraat");
        klant.setCity("Leegstad");
        klant.setCreatedBy("testuser");
        clientRepository.save(klant);

        AutoInvoiceRequestDTO dto = new AutoInvoiceRequestDTO();
        dto.setClientId(klant.getId());

        mockMvc.perform(post("/api/invoices/auto-generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Geen ongefactureerde uren gevonden voor clientId=" + klant.getId()));
    }

    @Test
    @WithMockUser(username = "peter")
    public void testGetInvoice_nonExistingInvoice_returns404() throws Exception {
        mockMvc.perform(get("/api/invoices/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Factuur met ID 99 niet gevonden"));
    }

    @Test
    @WithMockUser(username = "alice")
    public void testDeleteInvoice_otherUserInvoice_returns403() throws Exception {
        Invoice factuur = new Invoice();
        factuur.setInvoiceNumber("INV-123");
        factuur.setSenderName("Bob");
        factuur.setReceiverName("Klant");
        factuur.setReceiverEmail("klant@example.com");
        factuur.setIssueDate(LocalDate.now());
        factuur.setDueDate(LocalDate.now().plusDays(14));
        factuur.setStatus(InvoiceStatus.UNPAID);
        factuur.setAmount(BigDecimal.valueOf(100));
        factuur.setCreatedBy("bob");
        invoiceRepository.save(factuur);

        mockMvc.perform(delete("/api/invoices/" + factuur.getId()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Geen toegang tot factuur met ID " + factuur.getId()));
    }

}
