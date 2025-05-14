package com.example.zorgmate.service.interfaces;

import com.example.zorgmate.dal.entity.Invoice.InvoiceStatus;
import com.example.zorgmate.dto.Invoice.CreateInvoiceRequestDTO;
import com.example.zorgmate.dto.Invoice.GenerateInvoiceRequestDTO;
import com.example.zorgmate.dto.Invoice.InvoiceResponseDTO;

import java.util.List;

public interface InvoiceService {
    InvoiceResponseDTO getInvoiceById(Long id);
    List<InvoiceResponseDTO> getAllInvoices();
    List<InvoiceResponseDTO> getInvoicesByStatus(InvoiceStatus status);
    InvoiceResponseDTO updateInvoice(Long id, CreateInvoiceRequestDTO dto);
    void updateInvoiceStatus(Long id, InvoiceStatus status);
    void deleteInvoice(Long id);
    InvoiceResponseDTO autoGenerateInvoiceFromUnbilled(Long clientId);
}
