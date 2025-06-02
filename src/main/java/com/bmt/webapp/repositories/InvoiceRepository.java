package com.bmt.webapp.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bmt.webapp.models.Invoice;

public interface InvoiceRepository extends JpaRepository<Invoice, Integer> {
}
