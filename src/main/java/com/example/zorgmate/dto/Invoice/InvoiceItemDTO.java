package com.example.zorgmate.dto.Invoice;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InvoiceItemDTO {

    @NotBlank(message = "Beschrijving mag niet leeg zijn")
    private String description;

    @Min(value = 1, message = "Gewerkte uren moeten minimaal 1 zijn")
    private int hoursWorked;

    @DecimalMin(value = "0.01", message = "Uurtarief moet minimaal 0.01 zijn")
    private BigDecimal hourlyRate;

    @DecimalMin(value = "0.01", message = "Subtotaal moet minimaal 0.01 zijn")
    private BigDecimal subTotal;

    private LocalDate date;
}
