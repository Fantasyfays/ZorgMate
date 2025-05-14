package com.example.zorgmate.dto.timeentry;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class TimeEntryResponseDTO {
    private Long id;
    private String description;
    private int hours;
    private BigDecimal hourlyRate;
    private LocalDate date;
    private String clientName;
    private String projectName;
}
