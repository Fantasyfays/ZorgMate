package com.example.zorgmate.service.interfaces;

import com.example.zorgmate.dal.entity.Invoice.TimeEntry;
import com.example.zorgmate.dto.timeentry.TimeEntryCreateDTO;
import com.example.zorgmate.dto.timeentry.TimeEntryResponseDTO;

import java.util.List;

public interface TimeEntryService {
    TimeEntryResponseDTO createTimeEntry(TimeEntryCreateDTO dto);
    List<TimeEntryResponseDTO> getAllTimeEntries();
    List<TimeEntryResponseDTO> getUnbilledEntriesByClient(Long clientId);
    List<TimeEntry> findByInvoiceId(Long invoiceId);
}
