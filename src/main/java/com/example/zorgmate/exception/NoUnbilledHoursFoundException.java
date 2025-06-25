package com.example.zorgmate.exception;

public class NoUnbilledHoursFoundException extends RuntimeException {
    public NoUnbilledHoursFoundException(Long clientId) {
        super("Geen ongefactureerde uren gevonden voor clientId=" + clientId);
    }
}
