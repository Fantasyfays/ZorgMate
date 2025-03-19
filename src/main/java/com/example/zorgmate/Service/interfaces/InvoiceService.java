package com.example.zorgmate.Service.interfaces;

import com.example.zorgmate.dal.entity.InvoiceStatus;
import com.example.zorgmate.dto.Invoice.CreateInvoiceRequestDTO;
import com.example.zorgmate.dto.Invoice.InvoiceResponseDTO;

import java.util.List;

public interface InvoiceService {
    InvoiceResponseDTO createInvoice(CreateInvoiceRequestDTO dto);
    List<InvoiceResponseDTO> getAllInvoices();
    List<InvoiceResponseDTO> getInvoicesByStatus(InvoiceStatus status);
    InvoiceResponseDTO updateInvoice(Long id, CreateInvoiceRequestDTO dto);  // ✅ Update factuur
    void deleteInvoice(Long id);
    void updateInvoiceStatus(Long id, InvoiceStatus status);

}
