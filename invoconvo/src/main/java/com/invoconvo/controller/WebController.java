package com.invoconvo.controller;

import com.invoconvo.model.*;
import com.invoconvo.service.CustomerService;
import com.invoconvo.service.InvoiceService;
import com.invoconvo.service.PdfService;
import com.invoconvo.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class WebController {

    private final InvoiceService invoiceService;
    private final CustomerService customerService;
    private final ProductService productService;
    private final PdfService pdfService;

    @GetMapping("/")
    public String home() {
        return "landing";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("totalInvoices", invoiceService.totalInvoices());
        model.addAttribute("totalRevenue", invoiceService.totalRevenue());
        model.addAttribute("collected", invoiceService.amountCollected());
        model.addAttribute("outstanding", invoiceService.outstanding());
        model.addAttribute("overdueCount", invoiceService.countByStatus(InvoiceStatus.OVERDUE));
        model.addAttribute("draftCount", invoiceService.countByStatus(InvoiceStatus.DRAFT));
        model.addAttribute("recentInvoices", invoiceService.findAll().stream().limit(5).toList());
        return "dashboard";
    }

    @GetMapping("/invoices")
    public String listInvoices(@RequestParam(required = false) String q,
                               @RequestParam(required = false) String status,
                               Model model) {
        List<Invoice> invoices;
        if (status != null && !status.isBlank()) {
            invoices = invoiceService.filterByStatus(InvoiceStatus.valueOf(status));
        } else if (q != null && !q.isBlank()) {
            invoices = invoiceService.search(q);
        } else {
            invoices = invoiceService.findAll();
        }
        model.addAttribute("invoices", invoices);
        model.addAttribute("q", q);
        model.addAttribute("status", status);
        model.addAttribute("statuses", InvoiceStatus.values());
        return "invoices/list";
    }

    @GetMapping("/invoices/new")
    public String newInvoice(Model model) {
        Invoice invoice = new Invoice();
        invoice.setInvoiceDate(LocalDate.now());
        invoice.setDueDate(LocalDate.now().plusDays(30));
        invoice.setStatus(InvoiceStatus.DRAFT);
        invoice.setTerms("Payment due within 14 days of invoice date. Goods once sold will not be taken back. All disputes subject to local jurisdiction.");
        invoice.setPaymentMethod("UPI");
        invoice.setPaymentInstructions("UPI: sundaram@okaxis");
        invoice.setItems(new ArrayList<>());
        invoice.addItem(InvoiceItem.builder().quantity(BigDecimal.ONE).unitPrice(BigDecimal.ZERO).taxRate(new BigDecimal("12")).build());
        model.addAttribute("invoice", invoice);
        model.addAttribute("customers", customerService.findAll());
        model.addAttribute("products", productService.findAll());
        model.addAttribute("statuses", InvoiceStatus.values());
        return "invoices/form";
    }

    @GetMapping("/invoices/{id}")
    public String viewInvoice(@PathVariable Long id, Model model) {
        model.addAttribute("invoice", invoiceService.findById(id));
        return "invoices/view";
    }

    @GetMapping("/invoices/{id}/edit")
    public String editInvoice(@PathVariable Long id, Model model) {
        model.addAttribute("invoice", invoiceService.findById(id));
        model.addAttribute("customers", customerService.findAll());
        model.addAttribute("products", productService.findAll());
        model.addAttribute("statuses", InvoiceStatus.values());
        return "invoices/form";
    }

    @PostMapping("/invoices")
    public String saveInvoice(@ModelAttribute Invoice invoice,
                              @RequestParam(required = false) Long customerId,
                              RedirectAttributes ra) {
        if (customerId != null) {
            invoice.setCustomer(customerService.findById(customerId));
        }
        if (invoice.getId() == null) {
            invoiceService.create(invoice);
            ra.addFlashAttribute("success", "Invoice created successfully!");
        } else {
            invoiceService.update(invoice.getId(), invoice);
            ra.addFlashAttribute("success", "Invoice updated successfully!");
        }
        return "redirect:/invoices";
    }

    @PostMapping("/invoices/{id}/status")
    public String updateStatus(@PathVariable Long id, @RequestParam InvoiceStatus status, RedirectAttributes ra) {
        invoiceService.updateStatus(id, status);
        ra.addFlashAttribute("success", "Status updated to " + status);
        return "redirect:/invoices/" + id;
    }

    @PostMapping("/invoices/{id}/duplicate")
    public String duplicate(@PathVariable Long id, RedirectAttributes ra) {
        Invoice copy = invoiceService.duplicate(id);
        ra.addFlashAttribute("success", "Invoice duplicated as " + copy.getInvoiceNumber());
        return "redirect:/invoices/" + copy.getId() + "/edit";
    }

    @PostMapping("/invoices/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        invoiceService.delete(id);
        ra.addFlashAttribute("success", "Invoice deleted");
        return "redirect:/invoices";
    }

    @GetMapping("/invoices/{id}/pdf")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long id) {
        Invoice invoice = invoiceService.findById(id);
        byte[] pdf = pdfService.generateInvoicePdf(invoice);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=invoice-" + invoice.getInvoiceNumber() + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    // Customers
    @GetMapping("/customers")
    public String listCustomers(Model model) {
        model.addAttribute("customers", customerService.findAll());
        return "customers/list";
    }

    @GetMapping("/customers/new")
    public String newCustomer(Model model) {
        model.addAttribute("customer", new Customer());
        return "customers/form";
    }

    @GetMapping("/customers/{id}/edit")
    public String editCustomer(@PathVariable Long id, Model model) {
        model.addAttribute("customer", customerService.findById(id));
        return "customers/form";
    }

    @PostMapping("/customers")
    public String saveCustomer(@ModelAttribute Customer customer, RedirectAttributes ra) {
        customerService.save(customer);
        ra.addFlashAttribute("success", "Customer saved");
        return "redirect:/customers";
    }

    @PostMapping("/customers/{id}/delete")
    public String deleteCustomer(@PathVariable Long id, RedirectAttributes ra) {
        customerService.delete(id);
        ra.addFlashAttribute("success", "Customer deleted");
        return "redirect:/customers";
    }

    // Products
    @GetMapping("/products")
    public String listProducts(Model model) {
        model.addAttribute("products", productService.findAll());
        return "products/list";
    }

    @GetMapping("/products/new")
    public String newProduct(Model model) {
        model.addAttribute("product", new Product());
        return "products/form";
    }

    @GetMapping("/products/{id}/edit")
    public String editProduct(@PathVariable Long id, Model model) {
        model.addAttribute("product", productService.findById(id));
        return "products/form";
    }

    @PostMapping("/products")
    public String saveProduct(@ModelAttribute Product product, RedirectAttributes ra) {
        productService.save(product);
        ra.addFlashAttribute("success", "Product saved");
        return "redirect:/products";
    }

    @PostMapping("/products/{id}/delete")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes ra) {
        productService.delete(id);
        ra.addFlashAttribute("success", "Product deleted");
        return "redirect:/products";
    }
}
