package com.example.zorgmate.dto.Invoice;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class GenerateInvoiceRequestDTO {
    @NotNull(message = "Client ID is verplicht")
    private Long clientId;

    @NotBlank(message = "Factuurnummer mag niet leeg zijn")
    private String invoiceNumber;

    @NotBlank(message = "Ontvangernaam mag niet leeg zijn")
    private String receiverName;

    @NotBlank(message = "Verzendernaam mag niet leeg zijn")
    private String senderName;

    @NotBlank(message = "Vervaldatum mag niet leeg zijn")
    private String dueDate; // Overweeg `@Pattern` of `@DateTimeFormat` als dit altijd een bepaalde datumstructuur moet volgen
}
