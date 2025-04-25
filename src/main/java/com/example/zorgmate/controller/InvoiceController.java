package com.example.zorgmate.controller;

import com.example.zorgmate.service.interfaces.InvoiceService;
import com.example.zorgmate.dal.entity.Invoice.InvoiceStatus;
import com.example.zorgmate.dto.Invoice.CreateInvoiceRequestDTO;
import com.example.zorgmate.dto.Invoice.InvoiceResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<InvoiceResponseDTO> getInvoiceById(@PathVariable Long id) {
        try {
            InvoiceResponseDTO invoice = invoiceService.getInvoiceById(id);
            return ResponseEntity.ok(invoice);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);  // Return 404 status with empty body
        }
    }

    @PatchMapping("/{id}/status/{status}")
    public ResponseEntity<String> updateInvoiceStatus(@PathVariable Long id, @PathVariable InvoiceStatus status) {
        try {
            invoiceService.updateInvoiceStatus(id, status);
            return ResponseEntity.ok("Factuurstatus bijgewerkt naar: " + status);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Fout bij het bijwerken van de status.");
        }
    }

    @PostMapping
    public ResponseEntity<?> createInvoice(@Valid @RequestBody CreateInvoiceRequestDTO requestDTO, BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(getValidationErrors(result));
        }
        return ResponseEntity.ok(invoiceService.createInvoice(requestDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateInvoice(@PathVariable Long id, @Valid @RequestBody CreateInvoiceRequestDTO requestDTO, BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(getValidationErrors(result));
        }
        return ResponseEntity.ok(invoiceService.updateInvoice(id, requestDTO));
    }

    @GetMapping
    public ResponseEntity<List<InvoiceResponseDTO>> getAllInvoices() {
        return ResponseEntity.ok(invoiceService.getAllInvoices());
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<InvoiceResponseDTO>> getInvoicesByStatus(@PathVariable InvoiceStatus status) {
        return ResponseEntity.ok(invoiceService.getInvoicesByStatus(status));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteInvoice(@PathVariable Long id) {
        invoiceService.deleteInvoice(id);
        return ResponseEntity.ok("Factuur met ID " + id + " is verwijderd.");
    }

    private Map<String, String> getValidationErrors(BindingResult result) {
        // Controleer of er veldfouten zijn, zo niet retourneer een lege map
        if (result.getFieldErrors() == null || result.getFieldErrors().isEmpty()) {
            return new HashMap<>();  // Gebruik een lege HashMap om null te vermijden
        }

        return result.getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        FieldError::getDefaultMessage
                ));
    }

}
