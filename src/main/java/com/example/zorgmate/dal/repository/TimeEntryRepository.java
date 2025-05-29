package com.example.zorgmate.dal.repository;

import com.example.zorgmate.dal.entity.Invoice.TimeEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TimeEntryRepository extends JpaRepository<TimeEntry, Long> {
    List<TimeEntry> findByProjectId(Long projectId);
    List<TimeEntry> findByClientIdAndInvoiceIsNull(Long clientId);
    List<TimeEntry> findByCreatedBy(String createdBy);
    List<TimeEntry> findByClientIdAndInvoiceIsNullAndCreatedBy(Long clientId, String createdBy);
    List<TimeEntry> findByInvoiceId(Long invoiceId);
}
