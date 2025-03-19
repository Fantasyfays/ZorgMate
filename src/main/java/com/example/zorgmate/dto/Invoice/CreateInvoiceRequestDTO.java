package com.example.zorgmate.dto.Invoice;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateInvoiceRequestDTO {

    @NotBlank(message = "Factuurnummer mag niet leeg zijn")
    private String invoiceNumber;

    @NotBlank(message = "Naam van de verzender mag niet leeg zijn")
    private String senderName;

    @NotBlank(message = "Naam van de ontvanger mag niet leeg zijn")
    private String receiverName;

    @NotNull(message = "Uitgavedatum is verplicht")
    @FutureOrPresent(message = "Uitgavedatum kan niet in het verleden liggen")
    private LocalDate issueDate;

    @NotNull(message = "Vervaldatum is verplicht")
    @Future(message = "Vervaldatum moet in de toekomst liggen")
    private LocalDate dueDate;

    @NotNull(message = "Status is verplicht")
    private String status;

    @NotNull(message = "Factuuritems mogen niet leeg zijn")
    @Size(min = 1, message = "Een factuur moet minstens één item bevatten")
    private List<@Valid InvoiceItemDTO> items;
}
