package com.example.zorgmate.controller;

import com.example.zorgmate.dto.Invoice.AutoInvoiceRequestDTO;
import com.example.zorgmate.dto.Invoice.GenerateInvoiceRequestDTO;
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

    @GetMapping
    public ResponseEntity<List<InvoiceResponseDTO>> getAllInvoices() {
        return ResponseEntity.ok(invoiceService.getAllInvoices());
    }

    @GetMapping("/{id}")
    public ResponseEntity<InvoiceResponseDTO> getInvoiceById(@PathVariable Long id) {
        return ResponseEntity.ok(invoiceService.getInvoiceById(id));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<InvoiceResponseDTO>> getInvoicesByStatus(@PathVariable InvoiceStatus status) {
        return ResponseEntity.ok(invoiceService.getInvoicesByStatus(status));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateInvoice(@PathVariable Long id,
                                           @Valid @RequestBody CreateInvoiceRequestDTO dto,
                                           BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(getValidationErrors(result));
        }
        return ResponseEntity.ok(invoiceService.updateInvoice(id, dto));
    }

    @PatchMapping("/{id}/status/{status}")
    public ResponseEntity<String> updateInvoiceStatus(@PathVariable Long id, @PathVariable InvoiceStatus status) {
        invoiceService.updateInvoiceStatus(id, status);
        return ResponseEntity.ok("Factuurstatus bijgewerkt naar: " + status);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteInvoice(@PathVariable Long id) {
        invoiceService.deleteInvoice(id);
        return ResponseEntity.ok("Factuur met ID " + id + " is verwijderd.");
    }

    @PostMapping("/auto-generate")
    public ResponseEntity<InvoiceResponseDTO> autoGenerateInvoice(@RequestBody AutoInvoiceRequestDTO dto) {
        return ResponseEntity.ok(invoiceService.autoGenerateInvoiceFromUnbilled(dto.getClientId()));
    }

    private Map<String, String> getValidationErrors(BindingResult result) {
        return result.getFieldErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
    }
}
