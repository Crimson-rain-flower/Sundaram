package com.invoconvo.service;

import com.invoconvo.model.Invoice;
import com.invoconvo.model.InvoiceItem;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

@Service
public class PdfService {

    private static final Color FOREST = new Color(45, 74, 62);      // deep green
    private static final Color TURMERIC = new Color(218, 165, 32);  // gold/turmeric
    private static final Color CREAM = new Color(250, 246, 237);
    private static final Color TERRACOTTA = new Color(180, 98, 72);
    private static final Color CHARCOAL = new Color(40, 40, 40);
    private static final Color MUTED = new Color(120, 110, 100);

    public byte[] generateInvoicePdf(Invoice invoice) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 40, 40, 40, 40);
            PdfWriter.getInstance(document, baos);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 26, FOREST);
            Font brandFont = FontFactory.getFont(FontFactory.HELVETICA, 10, TURMERIC);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, CHARCOAL);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10, CHARCOAL);
            Font smallFont = FontFactory.getFont(FontFactory.HELVETICA, 9, MUTED);
            Font whiteBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);

            // Brand header
            Paragraph brand = new Paragraph("SUNDARAM", titleFont);
            brand.setAlignment(Element.ALIGN_LEFT);
            document.add(brand);
            Paragraph tagline = new Paragraph("Rooted in Ritual. Crafted for Skin.", brandFont);
            document.add(tagline);
            document.add(new Paragraph(" "));

            Paragraph invTitle = new Paragraph("INVOICE", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, TERRACOTTA));
            document.add(invTitle);
            document.add(new Paragraph("No. " + invoice.getInvoiceNumber(), headerFont));
            document.add(new Paragraph(" "));

            // Seller & Customer
            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            infoTable.setWidths(new float[]{1, 1});

            PdfPCell sellerCell = new PdfPCell();
            sellerCell.setBorder(Rectangle.NO_BORDER);
            sellerCell.setBackgroundColor(CREAM);
            sellerCell.setPadding(12);
            sellerCell.addElement(new Paragraph("FROM", headerFont));
            sellerCell.addElement(new Paragraph(nullSafe(invoice.getSellerName()), normalFont));
            sellerCell.addElement(new Paragraph(nullSafe(invoice.getSellerEmail()), smallFont));
            sellerCell.addElement(new Paragraph(nullSafe(invoice.getSellerPhone()), smallFont));
            sellerCell.addElement(new Paragraph(nullSafe(invoice.getSellerAddress()), smallFont));
            infoTable.addCell(sellerCell);

            PdfPCell buyerCell = new PdfPCell();
            buyerCell.setBorder(Rectangle.NO_BORDER);
            buyerCell.setBackgroundColor(new Color(245, 240, 230));
            buyerCell.setPadding(12);
            buyerCell.addElement(new Paragraph("BILL TO", headerFont));
            if (invoice.getCustomer() != null) {
                buyerCell.addElement(new Paragraph(nullSafe(invoice.getCustomer().getName()), normalFont));
                buyerCell.addElement(new Paragraph(nullSafe(invoice.getCustomer().getEmail()), smallFont));
                buyerCell.addElement(new Paragraph(nullSafe(invoice.getCustomer().getPhone()), smallFont));
                buyerCell.addElement(new Paragraph(nullSafe(invoice.getCustomer().getAddress()), smallFont));
            }
            infoTable.addCell(buyerCell);
            document.add(infoTable);
            document.add(new Paragraph(" "));

            // Dates
            PdfPTable dateTable = new PdfPTable(3);
            dateTable.setWidthPercentage(100);
            dateTable.addCell(createLabelCell("Invoice Date", formatDate(invoice.getInvoiceDate()), normalFont, smallFont));
            dateTable.addCell(createLabelCell("Due Date", formatDate(invoice.getDueDate()), normalFont, smallFont));
            dateTable.addCell(createLabelCell("Status", invoice.getStatus().name(), normalFont, smallFont));
            document.add(dateTable);
            document.add(new Paragraph(" "));

            // Items
            PdfPTable itemsTable = new PdfPTable(5);
            itemsTable.setWidthPercentage(100);
            itemsTable.setWidths(new float[]{3.5f, 1, 1.3f, 1, 1.4f});

            String[] headers = {"Description", "Qty", "Unit Price", "Tax %", "Amount"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, whiteBold));
                cell.setBackgroundColor(FOREST);
                cell.setPadding(8);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                itemsTable.addCell(cell);
            }

            for (InvoiceItem item : invoice.getItems()) {
                itemsTable.addCell(createItemCell(item.getDescription(), normalFont, Element.ALIGN_LEFT));
                itemsTable.addCell(createItemCell(item.getQuantity().toString(), normalFont, Element.ALIGN_CENTER));
                itemsTable.addCell(createItemCell("₹" + item.getUnitPrice().toString(), normalFont, Element.ALIGN_RIGHT));
                itemsTable.addCell(createItemCell(item.getTaxRate() + "%", normalFont, Element.ALIGN_CENTER));
                itemsTable.addCell(createItemCell("₹" + item.getLineTotal().toString(), normalFont, Element.ALIGN_RIGHT));
            }
            document.add(itemsTable);
            document.add(new Paragraph(" "));

            // Totals
            PdfPTable totals = new PdfPTable(2);
            totals.setWidthPercentage(42);
            totals.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totals.setWidths(new float[]{1.5f, 1});

            totals.addCell(createTotalLabel("Subtotal", normalFont));
            totals.addCell(createTotalValue("₹" + invoice.getSubtotal(), normalFont));
            totals.addCell(createTotalLabel("Tax", normalFont));
            totals.addCell(createTotalValue("₹" + invoice.getTaxTotal(), normalFont));
            totals.addCell(createTotalLabel("Discount", normalFont));
            totals.addCell(createTotalValue("-₹" + (invoice.getDiscountAmount() != null ? invoice.getDiscountAmount() : BigDecimal.ZERO), normalFont));

            PdfPCell totalLabel = new PdfPCell(new Phrase("TOTAL", whiteBold));
            totalLabel.setBackgroundColor(FOREST);
            totalLabel.setPadding(8);
            totalLabel.setBorder(Rectangle.NO_BORDER);
            totals.addCell(totalLabel);
            PdfPCell totalVal = new PdfPCell(new Phrase("₹" + invoice.getTotal(), whiteBold));
            totalVal.setBackgroundColor(FOREST);
            totalVal.setPadding(8);
            totalVal.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totalVal.setBorder(Rectangle.NO_BORDER);
            totals.addCell(totalVal);
            document.add(totals);

            if (invoice.getNotes() != null && !invoice.getNotes().isBlank()) {
                document.add(new Paragraph(" "));
                document.add(new Paragraph("Notes", headerFont));
                document.add(new Paragraph(invoice.getNotes(), smallFont));
            }
            if (invoice.getTerms() != null && !invoice.getTerms().isBlank()) {
                document.add(new Paragraph(" "));
                document.add(new Paragraph("Terms & Conditions", headerFont));
                document.add(new Paragraph(invoice.getTerms(), smallFont));
            }
            if (invoice.getPaymentInstructions() != null && !invoice.getPaymentInstructions().isBlank()) {
                document.add(new Paragraph(" "));
                document.add(new Paragraph("Payment Instructions", headerFont));
                document.add(new Paragraph(invoice.getPaymentInstructions(), smallFont));
            }

            document.add(new Paragraph(" "));
            Paragraph thanks = new Paragraph("Thank you for trusting Sundaram.", FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 11, FOREST));
            thanks.setAlignment(Element.ALIGN_CENTER);
            document.add(thanks);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF", e);
        }
    }

    private PdfPCell createLabelCell(String label, String value, Font valueFont, Font labelFont) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(5);
        cell.addElement(new Paragraph(label, labelFont));
        cell.addElement(new Paragraph(value != null ? value : "-", valueFont));
        return cell;
    }

    private PdfPCell createItemCell(String text, Font font, int align) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "", font));
        cell.setPadding(6);
        cell.setHorizontalAlignment(align);
        cell.setBorderColor(new Color(200, 190, 175));
        return cell;
    }

    private PdfPCell createTotalLabel(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(4);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        return cell;
    }

    private PdfPCell createTotalValue(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(4);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        return cell;
    }

    private String nullSafe(String s) {
        return s != null ? s : "";
    }

    private String formatDate(java.time.LocalDate date) {
        return date != null ? date.format(DateTimeFormatter.ofPattern("dd MMM yyyy")) : "-";
    }
}
