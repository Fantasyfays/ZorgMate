package com.example.zorgmate.exception;

public class AccessDeniedToInvoiceException extends RuntimeException {
    public AccessDeniedToInvoiceException(Long id) {
        super("Geen toegang tot factuur met ID " + id);
    }
}
