package com.invoconvo.repository;

import com.invoconvo.model.Invoice;
import com.invoconvo.model.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);
    List<Invoice> findByCustomerId(Long customerId);
    List<Invoice> findByStatus(InvoiceStatus status);

    @Query("SELECT i FROM Invoice i WHERE LOWER(i.invoiceNumber) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(i.customer.name) LIKE LOWER(CONCAT('%', :q, '%'))")
    List<Invoice> search(@Param("q") String q);

    long countByStatus(InvoiceStatus status);
}
