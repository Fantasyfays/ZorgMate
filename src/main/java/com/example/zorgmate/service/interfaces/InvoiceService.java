package com.example.zorgmate.service.interfaces;

import com.example.zorgmate.dal.entity.Invoice.InvoiceStatus;
import com.example.zorgmate.dto.Invoice.CreateInvoiceRequestDTO;
import com.example.zorgmate.dto.Invoice.InvoiceResponseDTO;

import java.util.List;

public interface InvoiceService {

    List<InvoiceResponseDTO> getInvoicesForUser(String username);
    InvoiceResponseDTO getInvoiceByIdForUser(Long id, String username);
    InvoiceResponseDTO updateInvoiceForUser(Long id, CreateInvoiceRequestDTO dto, String username);
    void deleteInvoiceForUser(Long id, String username);
    void updateInvoiceStatusForUser(Long id, InvoiceStatus status, String username);
    InvoiceResponseDTO autoGenerateInvoiceFromUnbilled(Long clientId, String username);

}
