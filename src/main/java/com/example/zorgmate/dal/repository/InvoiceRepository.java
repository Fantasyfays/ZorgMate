package com.example.zorgmate.dal.repository;

import com.example.zorgmate.dal.entity.Invoice;
import com.example.zorgmate.dal.entity.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    List<Invoice> findByStatus(InvoiceStatus status);
}
