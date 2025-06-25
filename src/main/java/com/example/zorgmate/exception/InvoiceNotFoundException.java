package com.example.zorgmate.exception;

public class InvoiceNotFoundException extends RuntimeException {
    public InvoiceNotFoundException(Long id) {
        super("Factuur met ID " + id + " niet gevonden");
    }
}
