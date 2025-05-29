package com.example.zorgmate.dto.timeentry;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class TimeEntryCreateDTO {
    @NotBlank(message = "Beschrijving mag niet leeg zijn")
    private String description;

    @Min(value = 1, message = "Aantal uren moet minimaal 1 zijn")
    private int hours;

    @DecimalMin(value = "0.01", message = "Uurtarief moet minimaal 0.01 zijn")
    private BigDecimal hourlyRate;

    @NotNull(message = "Datum is verplicht")
    private LocalDate date;

    @NotNull(message = "Client ID is verplicht")
    private Long clientId;

    private Long projectId;
}
