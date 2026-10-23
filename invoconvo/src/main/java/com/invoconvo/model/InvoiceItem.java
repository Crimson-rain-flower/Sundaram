package com.invoconvo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "invoice_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @NotBlank
    private String description;

    @NotNull
    @Positive
    @Column(precision = 10, scale = 2)
    private BigDecimal quantity;

    @NotNull
    @PositiveOrZero
    @Column(precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @PositiveOrZero
    @Column(precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal taxRate = BigDecimal.ZERO;

    // Calculated fields
    public BigDecimal getLineTotal() {
        if (quantity == null || unitPrice == null) return BigDecimal.ZERO;
        return quantity.multiply(unitPrice);
    }

    public BigDecimal getTaxAmount() {
        return getLineTotal().multiply(taxRate != null ? taxRate : BigDecimal.ZERO)
                .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
    }

    public BigDecimal getTotalWithTax() {
        return getLineTotal().add(getTaxAmount());
    }
}
