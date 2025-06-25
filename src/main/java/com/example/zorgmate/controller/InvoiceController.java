package com.example.zorgmate.controller;

import com.example.zorgmate.dto.Invoice.AutoInvoiceRequestDTO;
import com.example.zorgmate.dto.Invoice.CreateInvoiceRequestDTO;
import com.example.zorgmate.dto.Invoice.InvoiceResponseDTO;
import com.example.zorgmate.service.interfaces.InvoiceService;
import com.example.zorgmate.dal.entity.Invoice.InvoiceStatus;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.core.Authentication;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @GetMapping
    public ResponseEntity<List<InvoiceResponseDTO>> getAllInvoices(Authentication authentication) {
        String username = authentication.getName();
        return ResponseEntity.ok(invoiceService.getInvoicesForUser(username));
    }

    @GetMapping("/{id}")
    public ResponseEntity<InvoiceResponseDTO> getInvoiceById(@PathVariable Long id, Authentication authentication) {
        String username = authentication.getName();
        InvoiceResponseDTO response = invoiceService.getInvoiceByIdForUser(id, username);

        if (response == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateInvoice(@PathVariable Long id,
                                           @Valid @RequestBody CreateInvoiceRequestDTO dto,
                                           BindingResult result,
                                           Authentication authentication) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(getValidationErrors(result));
        }

        String username = authentication.getName();
        InvoiceResponseDTO updated = invoiceService.updateInvoiceForUser(id, dto, username);

        if (updated == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/status/{status}")
    public ResponseEntity<String> updateInvoiceStatus(@PathVariable Long id,
                                                      @PathVariable InvoiceStatus status,
                                                      Authentication authentication) {
        String username = authentication.getName();
        InvoiceResponseDTO invoice = invoiceService.getInvoiceByIdForUser(id, username);

        if (invoice == null) {
            return ResponseEntity.notFound().build();
        }

        invoiceService.updateInvoiceStatusForUser(id, status, username);
        return ResponseEntity.ok("Status bijgewerkt naar " + status);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInvoice(@PathVariable Long id, Authentication authentication) {
        String username = authentication.getName();
        invoiceService.deleteInvoiceForUser(id, username);
        return ResponseEntity.noContent().build(); // Als er geen exception gegooid wordt, is het verwijderd
    }



    @PostMapping("/auto-generate")
    public ResponseEntity<?> autoGenerateInvoice(@Valid @RequestBody AutoInvoiceRequestDTO dto,
                                                 Authentication authentication) {
        String username = authentication.getName();
        InvoiceResponseDTO response = invoiceService.autoGenerateInvoiceFromUnbilled(dto.getClientId(), username);

        if (response == null) {
            return ResponseEntity.badRequest().body("Geen ongefactureerde uren gevonden.");
        }

        return ResponseEntity.ok(response);
    }


    private Map<String, String> getValidationErrors(BindingResult result) {
        return result.getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        error -> Objects.requireNonNullElse(error.getDefaultMessage(), "Onbekende fout")
                ));
    }

}
