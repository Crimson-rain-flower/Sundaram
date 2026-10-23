package com.invoconvo.service;

import com.invoconvo.model.*;
import com.invoconvo.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
@Transactional
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final CustomerService customerService;

    @Value("${app.company.name:Invoconvo}")
    private String companyName;
    @Value("${app.company.email:hello@invoconvo.com}")
    private String companyEmail;
    @Value("${app.company.phone:+1-555-0100}")
    private String companyPhone;
    @Value("${app.company.address:123 Business Avenue}")
    private String companyAddress;

    private final AtomicLong sequence = new AtomicLong(1000);

    public String generateInvoiceNumber() {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        long seq = sequence.incrementAndGet();
        return "INV-" + datePart + "-" + String.format("%04d", seq % 10000);
    }

    public List<Invoice> findAll() {
        return invoiceRepository.findAll();
    }

    public Invoice findById(Long id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invoice not found: " + id));
    }

    public Invoice findByNumber(String number) {
        return invoiceRepository.findByInvoiceNumber(number)
                .orElseThrow(() -> new RuntimeException("Invoice not found: " + number));
    }

    public Invoice create(Invoice invoice) {
        if (invoice.getInvoiceNumber() == null || invoice.getInvoiceNumber().isBlank()) {
            invoice.setInvoiceNumber(generateInvoiceNumber());
        }
        if (invoice.getSellerName() == null) {
            invoice.setSellerName(companyName);
            invoice.setSellerEmail(companyEmail);
            invoice.setSellerPhone(companyPhone);
            invoice.setSellerAddress(companyAddress);
        }
        if (invoice.getStatus() == null) {
            invoice.setStatus(InvoiceStatus.DRAFT);
        }
        if (invoice.getTerms() == null || invoice.getTerms().isBlank()) {
            invoice.setTerms("Payment due within 14 days of invoice date. Goods once sold will not be taken back. All disputes subject to local jurisdiction.");
        }
        // re-link items
        if (invoice.getItems() != null) {
            invoice.getItems().forEach(item -> item.setInvoice(invoice));
        }
        return invoiceRepository.save(invoice);
    }

    public Invoice update(Long id, Invoice updated) {
        Invoice existing = findById(id);
        existing.setCustomer(updated.getCustomer());
        existing.setInvoiceDate(updated.getInvoiceDate());
        existing.setDueDate(updated.getDueDate());
        existing.setStatus(updated.getStatus());
        existing.setDiscountAmount(updated.getDiscountAmount());
        existing.setNotes(updated.getNotes());
        existing.setTerms(updated.getTerms());
        existing.setPaymentInstructions(updated.getPaymentInstructions());
        existing.setPaymentMethod(updated.getPaymentMethod());
        existing.setSellerName(updated.getSellerName());
        existing.setSellerEmail(updated.getSellerEmail());
        existing.setSellerPhone(updated.getSellerPhone());
        existing.setSellerAddress(updated.getSellerAddress());

        existing.getItems().clear();
        if (updated.getItems() != null) {
            updated.getItems().forEach(item -> {
                item.setId(null);
                existing.addItem(item);
            });
        }
        return invoiceRepository.save(existing);
    }

    public Invoice updateStatus(Long id, InvoiceStatus status) {
        Invoice inv = findById(id);
        inv.setStatus(status);
        return invoiceRepository.save(inv);
    }

    public void delete(Long id) {
        invoiceRepository.deleteById(id);
    }

    public Invoice duplicate(Long id) {
        Invoice original = findById(id);
        Invoice copy = Invoice.builder()
                .customer(original.getCustomer())
                .invoiceDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(30))
                .status(InvoiceStatus.DRAFT)
                .discountAmount(original.getDiscountAmount())
                .notes(original.getNotes())
                .terms(original.getTerms())
                .paymentInstructions(original.getPaymentInstructions())
                .paymentMethod(original.getPaymentMethod())
                .sellerName(original.getSellerName())
                .sellerEmail(original.getSellerEmail())
                .sellerPhone(original.getSellerPhone())
                .sellerAddress(original.getSellerAddress())
                .build();
        original.getItems().forEach(item -> {
            InvoiceItem newItem = InvoiceItem.builder()
                    .description(item.getDescription())
                    .quantity(item.getQuantity())
                    .unitPrice(item.getUnitPrice())
                    .taxRate(item.getTaxRate())
                    .build();
            copy.addItem(newItem);
        });
        return create(copy);
    }

    public List<Invoice> search(String query) {
        if (query == null || query.isBlank()) return findAll();
        return invoiceRepository.search(query);
    }

    public List<Invoice> filterByStatus(InvoiceStatus status) {
        return invoiceRepository.findByStatus(status);
    }

    // Dashboard stats
    public long totalInvoices() {
        return invoiceRepository.count();
    }

    public long countByStatus(InvoiceStatus status) {
        return invoiceRepository.countByStatus(status);
    }

    public BigDecimal totalRevenue() {
        return findAll().stream()
                .filter(i -> i.getStatus() == InvoiceStatus.PAID || i.getStatus() == InvoiceStatus.SENT)
                .map(Invoice::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal amountCollected() {
        return findAll().stream()
                .filter(i -> i.getStatus() == InvoiceStatus.PAID)
                .map(Invoice::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal outstanding() {
        return findAll().stream()
                .filter(i -> i.getStatus() == InvoiceStatus.SENT || i.getStatus() == InvoiceStatus.OVERDUE)
                .map(Invoice::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
