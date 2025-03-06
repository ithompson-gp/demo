package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import com.example.demo.service.DocxGeneratorService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

  @Autowired
  private DocxGeneratorService docxGeneratorService;

  @GetMapping("/invoice/{id}")
  public ResponseEntity<StreamingResponseBody> generateInvoice(@PathVariable String id) {
    // In a real application, you would retrieve the data from a database, stream, event, etc.
    Map<String, Object> data = createSampleInvoiceData(id);

    // Create a streaming response
    StreamingResponseBody responseBody = outputStream -> {
      try {
        docxGeneratorService.generateDocx("invoice-template", data, outputStream);
      } catch (Exception e) {
        throw new RuntimeException("Error generating invoice", e);
      }
    };

    // Set appropriate headers
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
    headers.setContentDispositionFormData("attachment", "Invoice-" + id + ".docx");

    return ResponseEntity.ok()
        .headers(headers)
        .body(responseBody);
  }

  private Map<String, Object> createSampleInvoiceData(String id) {
    Map<String, Object> data = new HashMap<>();

    // Basic information
    data.put("title", "Invoice #" + id);
    data.put("documentId", id);
    data.put("generatedDate", LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));

    // Client information
    Map<String, String> client = new HashMap<>();
    client.put("name", "John Smith");
    client.put("email", "john.smith@example.com");
    client.put("phone", "+1 (555) 123-4567");
    data.put("client", client);

    // Items
    List<Map<String, Object>> items = new ArrayList<>();

    Map<String, Object> item1 = new HashMap<>();
    item1.put("id", "PROD-001");
    item1.put("name", "Product A");
    item1.put("quantity", 2);
    item1.put("price", 29.99);
    item1.put("total", 59.98);
    items.add(item1);

    Map<String, Object> item2 = new HashMap<>();
    item2.put("id", "PROD-002");
    item2.put("name", "Product B");
    item2.put("quantity", 1);
    item2.put("price", 49.99);
    item2.put("total", 49.99);
    items.add(item2);

    data.put("items", items);

    // Summary
    data.put("totalAmount", 109.97);
    data.put("paymentStatus", "Paid");
    data.put("notes", "Thank you for your business!");

    return data;
  }
}
