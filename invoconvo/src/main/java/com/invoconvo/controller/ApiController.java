package com.invoconvo.controller;

import com.invoconvo.model.*;
import com.invoconvo.service.CustomerService;
import com.invoconvo.service.InvoiceService;
import com.invoconvo.service.PdfService;
import com.invoconvo.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Invoconvo API", description = "REST API for Invoice Generator")
public class ApiController {

    private final InvoiceService invoiceService;
    private final CustomerService customerService;
    private final ProductService productService;
    private final PdfService pdfService;

    // Invoices
    @GetMapping("/invoices")
    @Operation(summary = "List all invoices")
    public List<Invoice> listInvoices(@RequestParam(required = false) String status) {
        if (status != null) {
            return invoiceService.filterByStatus(InvoiceStatus.valueOf(status));
        }
        return invoiceService.findAll();
    }

    @GetMapping("/invoices/{id}")
    @Operation(summary = "Get invoice by ID")
    public Invoice getInvoice(@PathVariable Long id) {
        return invoiceService.findById(id);
    }

    @PostMapping("/invoices")
    @Operation(summary = "Create invoice")
    public Invoice createInvoice(@RequestBody Invoice invoice) {
        return invoiceService.create(invoice);
    }

    @PutMapping("/invoices/{id}")
    @Operation(summary = "Update invoice")
    public Invoice updateInvoice(@PathVariable Long id, @RequestBody Invoice invoice) {
        return invoiceService.update(id, invoice);
    }

    @PatchMapping("/invoices/{id}/status")
    @Operation(summary = "Update invoice status")
    public Invoice updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return invoiceService.updateStatus(id, InvoiceStatus.valueOf(body.get("status")));
    }

    @DeleteMapping("/invoices/{id}")
    @Operation(summary = "Delete invoice")
    public ResponseEntity<Void> deleteInvoice(@PathVariable Long id) {
        invoiceService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/invoices/{id}/pdf")
    @Operation(summary = "Download invoice PDF")
    public ResponseEntity<byte[]> pdf(@PathVariable Long id) {
        Invoice inv = invoiceService.findById(id);
        byte[] pdf = pdfService.generateInvoicePdf(inv);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + inv.getInvoiceNumber() + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    // Customers
    @GetMapping("/customers")
    public List<Customer> listCustomers() {
        return customerService.findAll();
    }

    @PostMapping("/customers")
    public Customer createCustomer(@RequestBody Customer customer) {
        return customerService.save(customer);
    }

    @GetMapping("/customers/{id}")
    public Customer getCustomer(@PathVariable Long id) {
        return customerService.findById(id);
    }

    // Products
    @GetMapping("/products")
    public List<Product> listProducts() {
        return productService.findActive();
    }

    @PostMapping("/products")
    public Product createProduct(@RequestBody Product product) {
        return productService.save(product);
    }

    // Dashboard
    @GetMapping("/dashboard")
    @Operation(summary = "Dashboard statistics")
    public Map<String, Object> dashboard() {
        return Map.of(
                "totalInvoices", invoiceService.totalInvoices(),
                "totalRevenue", invoiceService.totalRevenue(),
                "amountCollected", invoiceService.amountCollected(),
                "outstanding", invoiceService.outstanding(),
                "overdueCount", invoiceService.countByStatus(InvoiceStatus.OVERDUE)
        );
    }
}
