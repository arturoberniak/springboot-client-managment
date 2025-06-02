package com.bmt.webapp.controllers;

import com.bmt.webapp.exception.ClientNotFoundException;
import com.bmt.webapp.exception.InvoiceNotFoundException;
import com.bmt.webapp.models.Client;
import com.bmt.webapp.models.Invoice;
import com.bmt.webapp.repositories.ClientRepository;
import com.bmt.webapp.repositories.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.NoSuchElementException;

@Controller
@RequestMapping("/clients")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceRepository invoiceRepo;
    private final ClientRepository clientRepo;

    @PostMapping("/details")
    public String addInvoice(@RequestParam int id, @RequestParam MultipartFile file) throws IOException {

        if (file == null) {
            return "redirect:/clients/details?id=" + id;
        }

        Client client = clientRepo.findById(id)
                .orElseThrow(() -> new ClientNotFoundException("Client not found with id " + id));

        Date createdAt = new Date();
        String fileName = file.getOriginalFilename();
        String extension = fileName.substring(fileName.lastIndexOf('.'));
        String storageFileName = createdAt.getTime() + extension;
        String uploadDir = "storage/invoices/";
        Path uploadPath = Paths.get(uploadDir);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, Paths.get(uploadDir + storageFileName),
                    StandardCopyOption.REPLACE_EXISTING);
        }

        var invoice = new Invoice();
        invoice.setFileName(fileName);
        invoice.setStorageFileName(storageFileName);
        invoice.setCreatedAt(createdAt);
        invoice.setClient(client);

        client.getInvoices().add(invoice);
        clientRepo.save(client);

        return "redirect:/clients/details?id=" + id;
    }

    @GetMapping("/invoices")
    public ResponseEntity<Object> getInvoice(@RequestParam int invoiceId) throws FileNotFoundException {

        Invoice invoice = invoiceRepo.findById(invoiceId)
                .orElseThrow(() -> new InvoiceNotFoundException("Invoice not found with id " + invoiceId));

        File file = new File("storage/invoices/" + invoice.getStorageFileName());
        InputStreamResource resource = new InputStreamResource(
                new FileInputStream(file));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + invoice.getFileName() + "\"")
                .contentLength(file.length())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }


    @GetMapping("/invoices/delete")
    public String deleteInvoice(@RequestParam int clientId, @RequestParam int invoiceId) throws IOException {

        Invoice invoice = invoiceRepo.findById(invoiceId)
                .orElseThrow(() -> new InvoiceNotFoundException("Invoice not found with id " + invoiceId));

        Path filePath = Paths.get("storage/invoices/" + invoice.getStorageFileName());
        Files.delete(filePath);

        invoiceRepo.delete(invoice);

        return "redirect:/clients/details?id=" + clientId;
    }
}
