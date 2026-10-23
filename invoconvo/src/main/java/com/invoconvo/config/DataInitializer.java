package com.invoconvo.config;

import com.invoconvo.model.*;
import com.invoconvo.repository.CustomerRepository;
import com.invoconvo.repository.ProductRepository;
import com.invoconvo.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final InvoiceService invoiceService;

    @Override
    public void run(String... args) {
        if (customerRepository.count() > 0) return;

        // Customers
        Customer c1 = customerRepository.save(Customer.builder()
                .name("Ananya Krishnan")
                .email("ananya.krishnan@email.com")
                .phone("+91 98765 11001")
                .address("12, Lotus Avenue, Jayanagar")
                .city("Bengaluru").country("India")
                .build());

        Customer c2 = customerRepository.save(Customer.builder()
                .name("Meera Nair")
                .email("meera.nair@email.com")
                .phone("+91 98200 33445")
                .address("45, Marine Drive")
                .city("Mumbai").country("India")
                .build());

        Customer c3 = customerRepository.save(Customer.builder()
                .name("Priya Sharma")
                .email("priya.sharma@email.com")
                .phone("+91 98111 22334")
                .address("78, Green Park Extension")
                .city("New Delhi").country("India")
                .build());

        Customer c4 = customerRepository.save(Customer.builder()
                .name("Kavya Reddy")
                .email("kavya.reddy@email.com")
                .phone("+91 90000 55667")
                .address("23, Jubilee Hills")
                .city("Hyderabad").country("India")
                .build());

        Customer c5 = customerRepository.save(Customer.builder()
                .name("Divya Menon")
                .email("divya.menon@email.com")
                .phone("+91 97440 88990")
                .address("9, MG Road, Ernakulam")
                .city("Kochi").country("India")
                .build());

        // Skincare Products (₹)
        Product p1 = productRepository.save(Product.builder()
                .name("Ruchi Haldi Face Mask")
                .description("Pure turmeric & sandalwood brightening mask")
                .unitPrice(new BigDecimal("449.00")).taxRate(new BigDecimal("12")).unit("jar").active(true).build());

        Product p2 = productRepository.save(Product.builder()
                .name("Surya Brightening Mask")
                .description("Saffron, orange peel & vitamin C glow mask")
                .unitPrice(new BigDecimal("599.00")).taxRate(new BigDecimal("12")).unit("jar").active(true).build());

        Product p3 = productRepository.save(Product.builder()
                .name("Rose Shower Gel")
                .description("Damask rose & honey cleansing gel")
                .unitPrice(new BigDecimal("379.00")).taxRate(new BigDecimal("12")).unit("bottle").active(true).build());

        Product p4 = productRepository.save(Product.builder()
                .name("Neem & Tulsi Face Wash")
                .description("Antibacterial daily cleanser")
                .unitPrice(new BigDecimal("299.00")).taxRate(new BigDecimal("12")).unit("bottle").active(true).build());

        Product p5 = productRepository.save(Product.builder()
                .name("Kumkumadi Night Serum")
                .description("Ayurvedic 22-oil radiance serum")
                .unitPrice(new BigDecimal("899.00")).taxRate(new BigDecimal("12")).unit("bottle").active(true).build());

        Product p6 = productRepository.save(Product.builder()
                .name("Ashwagandha Body Butter")
                .description("Deep nourishing whipped body butter")
                .unitPrice(new BigDecimal("549.00")).taxRate(new BigDecimal("12")).unit("jar").active(true).build());

        Product p7 = productRepository.save(Product.builder()
                .name("Sandalwood Cooling Toner")
                .description("Alcohol-free mist toner")
                .unitPrice(new BigDecimal("349.00")).taxRate(new BigDecimal("12")).unit("bottle").active(true).build());

        Product p8 = productRepository.save(Product.builder()
                .name("Jasmine Hair Oil")
                .description("Cold-pressed herbal hair oil")
                .unitPrice(new BigDecimal("429.00")).taxRate(new BigDecimal("12")).unit("bottle").active(true).build());

        // Sample invoices
        Invoice inv1 = Invoice.builder()
                .customer(c1)
                .invoiceDate(LocalDate.now().minusDays(3))
                .dueDate(LocalDate.now().plusDays(11))
                .status(InvoiceStatus.SENT)
                .notes("Thank you for choosing Sundaram. May your skin glow with the seasons.")
                .terms("Payment due within 14 days. Goods once sold will not be taken back.")
                .paymentInstructions("UPI: sundaram@okaxis | Bank: HDFC 50200012345678")
                .discountAmount(new BigDecimal("50.00"))
                .build();
        inv1.addItem(InvoiceItem.builder().description(p1.getName()).quantity(new BigDecimal("2")).unitPrice(p1.getUnitPrice()).taxRate(p1.getTaxRate()).build());
        inv1.addItem(InvoiceItem.builder().description(p3.getName()).quantity(new BigDecimal("1")).unitPrice(p3.getUnitPrice()).taxRate(p3.getTaxRate()).build());
        inv1.addItem(InvoiceItem.builder().description(p5.getName()).quantity(new BigDecimal("1")).unitPrice(p5.getUnitPrice()).taxRate(p5.getTaxRate()).build());
        invoiceService.create(inv1);

        Invoice inv2 = Invoice.builder()
                .customer(c2)
                .invoiceDate(LocalDate.now().minusDays(8))
                .dueDate(LocalDate.now().minusDays(1))
                .status(InvoiceStatus.OVERDUE)
                .notes("Gentle reminder for pending payment.")
                .terms("Payment due within 14 days.")
                .paymentInstructions("UPI: sundaram@okaxis")
                .build();
        inv2.addItem(InvoiceItem.builder().description(p2.getName()).quantity(new BigDecimal("3")).unitPrice(p2.getUnitPrice()).taxRate(p2.getTaxRate()).build());
        inv2.addItem(InvoiceItem.builder().description(p6.getName()).quantity(new BigDecimal("2")).unitPrice(p6.getUnitPrice()).taxRate(p6.getTaxRate()).build());
        invoiceService.create(inv2);

        Invoice inv3 = Invoice.builder()
                .customer(c3)
                .invoiceDate(LocalDate.now().minusDays(15))
                .dueDate(LocalDate.now().minusDays(1))
                .status(InvoiceStatus.PAID)
                .notes("Thank you!")
                .build();
        inv3.addItem(InvoiceItem.builder().description(p4.getName()).quantity(new BigDecimal("4")).unitPrice(p4.getUnitPrice()).taxRate(p4.getTaxRate()).build());
        inv3.addItem(InvoiceItem.builder().description(p7.getName()).quantity(new BigDecimal("2")).unitPrice(p7.getUnitPrice()).taxRate(p7.getTaxRate()).build());
        invoiceService.create(inv3);

        Invoice inv4 = Invoice.builder()
                .customer(c4)
                .invoiceDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(14))
                .status(InvoiceStatus.DRAFT)
                .build();
        inv4.addItem(InvoiceItem.builder().description(p8.getName()).quantity(new BigDecimal("1")).unitPrice(p8.getUnitPrice()).taxRate(p8.getTaxRate()).build());
        inv4.addItem(InvoiceItem.builder().description(p1.getName()).quantity(new BigDecimal("1")).unitPrice(p1.getUnitPrice()).taxRate(p1.getTaxRate()).build());
        invoiceService.create(inv4);

        Invoice inv5 = Invoice.builder()
                .customer(c5)
                .invoiceDate(LocalDate.now().minusDays(2))
                .dueDate(LocalDate.now().plusDays(12))
                .status(InvoiceStatus.SENT)
                .build();
        inv5.addItem(InvoiceItem.builder().description(p5.getName()).quantity(new BigDecimal("2")).unitPrice(p5.getUnitPrice()).taxRate(p5.getTaxRate()).build());
        inv5.addItem(InvoiceItem.builder().description(p3.getName()).quantity(new BigDecimal("3")).unitPrice(p3.getUnitPrice()).taxRate(p3.getTaxRate()).build());
        invoiceService.create(inv5);
    }
}
