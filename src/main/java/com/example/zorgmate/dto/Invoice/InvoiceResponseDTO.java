package com.example.zorgmate.dto.Invoice;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InvoiceResponseDTO {
    private Long id;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private int invoiceNumber;
    private String status;
    private BigDecimal totalAmount;
    private String senderName;
    private String receiverName;
    private List<InvoiceItemDTO> items;
}
