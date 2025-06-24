package com.example.zorgmate.controller;

import com.example.zorgmate.dal.entity.Client.Client;
import com.example.zorgmate.dal.entity.Invoice.Invoice;
import com.example.zorgmate.dal.entity.Invoice.InvoiceStatus;
import com.example.zorgmate.dal.entity.Invoice.TimeEntry;
import com.example.zorgmate.dal.repository.ClientRepository;
import com.example.zorgmate.dal.repository.InvoiceRepository;
import com.example.zorgmate.dal.repository.TimeEntryRepository;
import com.example.zorgmate.dto.Invoice.CreateInvoiceRequestDTO;
import com.example.zorgmate.dto.Invoice.InvoiceItemDTO;
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

import static org.hamcrest.Matchers.containsString;
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
        // Maak klant aan
        Client klant = new Client();
        klant.setName("Jan");
        klant.setEmail("jan@mail.com");
        klant.setPhone("0612345678");
        klant.setPostcode("1234AB");
        klant.setHouseNumber("12");
        klant.setStreet("Hoofdstraat");
        klant.setCity("Amsterdam");
        klant.setCreatedBy("user1");
        clientRepository.save(klant);

        // Voeg uren toe
        TimeEntry uren = new TimeEntry();
        uren.setClient(klant);
        uren.setHours(5);
        uren.setHourlyRate(BigDecimal.valueOf(50));
        uren.setDescription("Werk");
        uren.setCreatedBy("user1");
        uren.setDate(LocalDate.now());
        timeEntryRepository.save(uren);

        // Maak body als JSON-string
        String jsonBody = "{ \"clientId\": " + klant.getId() + " }";

        // Doe de POST call en controleer response
        mockMvc.perform(post("/api/invoices/auto-generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"receiverName\":\"Jan\"")))
                .andExpect(content().string(containsString("\"totalAmount\":250")));
    }

    @Test
    @WithMockUser(username = "testuser")
    public void testAutoGenerateInvoice_withoutHours_returns400() throws Exception {
        Client klant = new Client();
        klant.setName("LegeKlant");
        klant.setEmail("leeg@mail.com");
        klant.setPhone("0600000000");
        klant.setPostcode("0000AA");
        klant.setHouseNumber("1");
        klant.setStreet("LegeStraat");
        klant.setCity("Leegstad");
        klant.setCreatedBy("testuser");
        clientRepository.save(klant);

        String jsonBody = "{ \"clientId\": " + klant.getId() + " }";

        mockMvc.perform(post("/api/invoices/auto-generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "peter")
    public void testGetInvoice_nonExistingInvoice_returns404() throws Exception {
        mockMvc.perform(get("/api/invoices/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "alice")
    public void testDeleteInvoice_otherUserInvoice_returns403() throws Exception {
        Invoice factuur = new Invoice();
        factuur.setInvoiceNumber("INV-123");
        factuur.setSenderName("Bob");
        factuur.setReceiverName("Klant");
        factuur.setIssueDate(LocalDate.now());
        factuur.setDueDate(LocalDate.now().plusDays(14));
        factuur.setStatus(InvoiceStatus.UNPAID);
        factuur.setAmount(BigDecimal.valueOf(100));
        factuur.setCreatedBy("bob");
        invoiceRepository.save(factuur);

        mockMvc.perform(delete("/api/invoices/" + factuur.getId()))
                .andExpect(status().isForbidden());
    }


}
