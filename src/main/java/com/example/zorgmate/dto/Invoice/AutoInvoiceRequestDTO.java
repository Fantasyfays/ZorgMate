package com.example.zorgmate.dto.Invoice;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AutoInvoiceRequestDTO {
    @NotNull(message = "Client ID is verplicht")
    private Long clientId;
}
