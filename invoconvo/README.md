# Sundaram — Invoice Generator

Traditional-meets-modern skincare invoice studio built with **Spring Boot**.

**Brand:** Sundaram  
**Tagline:** Rooted in Ritual. Crafted for Skin.  
**Currency:** Indian Rupees (₹)

## Features

- Landing page with brand story
- Dashboard with revenue, collected, outstanding, overdue stats
- Create / edit / duplicate / delete invoices
- Auto invoice numbers, line items, tax, discount
- PDF download with branded layout
- Customer & product catalogues
- Status workflow: Draft → Sent → Paid / Overdue / Cancelled
- Search & filter invoices
- REST API + Swagger UI
- Form login (demo: admin / admin123)

## Sample Products
Ruchi Haldi Face Mask · Surya Brightening Mask · Rose Shower Gel · Neem & Tulsi Face Wash · Kumkumadi Night Serum · Ashwagandha Body Butter · Sandalwood Cooling Toner · Jasmine Hair Oil

## Run

```bash
cd invoconvo
bash mvnw spring-boot:run
```

Open http://localhost:8080  
Login: **admin** / **admin123**

## Docker
```bash
docker-compose up --build
```

## Stack
Spring Boot · JPA/H2 · Thymeleaf · Spring Security · OpenPDF · springdoc-openapi · Java 17
