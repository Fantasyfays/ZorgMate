package com.example.zorgmate;

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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class InvoiceControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private InvoiceRepository invoiceRepository;
    @Autowired private ClientRepository clientRepository;
    @Autowired private TimeEntryRepository timeEntryRepository;
    @Autowired private ObjectMapper objectMapper;

    // Test: factuur genereren (happy flow)
    @Test
    @WithMockUser(username = "user1")
    void factuurMakenMetUren_moetStatus200Geven() throws Exception {
        // Maak klant aan
        Client klant = clientRepository.save(new Client(null, "Jan", "jan@mail.com", "Straat", "1234AB"));

        // Voeg uren toe voor die klant
        timeEntryRepository.save(TimeEntry.builder()
                .client(klant)
                .hours(5)
                .hourlyRate(BigDecimal.valueOf(50))
                .description("Werk")
                .createdBy("user1")
                .date(LocalDate.now())
                .build());

        // Vraag om factuur aan te maken
        String body = objectMapper.writeValueAsString(Map.of("clientId", klant.getId()));

        mockMvc.perform(post("/api/invoices/auto-generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.receiverName").value("Jan"))
                .andExpect(jsonPath("$.totalAmount").value(250));
    }

    // Test: geen uren beschikbaar → foutmelding
    @Test
    @WithMockUser(username = "testuser")
    void factuurMakenZonderUren_moetStatus400Geven() throws Exception {
        Client klant = clientRepository.save(new Client(null, "LegeKlant", "leeg@mail.com", "Pad", "0000AA"));

        String body = objectMapper.writeValueAsString(Map.of("clientId", klant.getId()));

        mockMvc.perform(post("/api/invoices/auto-generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    // Test: factuur opvragen die niet bestaat
    @Test
    @WithMockUser(username = "peter")
    void factuurOpvragenDieNietBestaat_moetStatus404Geven() throws Exception {
        mockMvc.perform(get("/api/invoices/999999"))
                .andExpect(status().isNotFound());
    }

    // Test: factuur verwijderen die je niet gemaakt hebt
    @Test
    @WithMockUser(username = "alice")
    void factuurVerwijderenVanIemandAnders_moetStatus403Geven() throws Exception {
        Invoice factuur = invoiceRepository.save(Invoice.builder()
                .invoiceNumber("INV-123")
                .senderName("Bob")
                .receiverName("Klant")
                .issueDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(14))
                .status(InvoiceStatus.UNPAID)
                .amount(BigDecimal.valueOf(100))
                .createdBy("bob")
                .build());

        mockMvc.perform(delete("/api/invoices/" + factuur.getId()))
                .andExpect(status().isForbidden());
    }

    // Test: naam van klant mag maximaal 50 tekens zijn
    @Test
    @WithMockUser(username = "edgeuser")
    void updateFactuur_metMaxLangeNaam_moetStatus200Geven() throws Exception {
        Client klant = clientRepository.save(new Client(null, "EdgeKlant", "e@mail.com", "Straat", "9999ZZ"));

        // Voeg uren toe voor die klant
        timeEntryRepository.save(TimeEntry.builder()
                .client(klant)
                .hours(5)
                .hourlyRate(BigDecimal.valueOf(50))
                .description("Werk")
                .createdBy("edgeuser")
                .date(LocalDate.now())
                .build());

        // Maak factuur aan via API
        String body = objectMapper.writeValueAsString(Map.of("clientId", klant.getId()));
        String response = mockMvc.perform(post("/api/invoices/auto-generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andReturn().getResponse().getContentAsString();

        Long invoiceId = objectMapper.readTree(response).get("id").asLong();

        // Maak een DTO met precies 50 tekens in receiverName
        CreateInvoiceRequestDTO dto = new CreateInvoiceRequestDTO();
        dto.setInvoiceNumber("INV-1234");
        dto.setSenderName("edgeuser");
        dto.setReceiverName("A".repeat(50));
        dto.setIssueDate(LocalDate.now());
        dto.setDueDate(LocalDate.now().plusDays(14));
        dto.setStatus("UNPAID");
        dto.setItems(List.of(
                InvoiceItemDTO.builder()
                        .description("werk")
                        .hoursWorked(2)
                        .hourlyRate(BigDecimal.valueOf(60))
                        .subTotal(BigDecimal.valueOf(120))
                        .build()
        ));

        // Voer update uit
        mockMvc.perform(put("/api/invoices/" + invoiceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.receiverName").value("A".repeat(50)));
    }
}
