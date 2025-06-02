package com.bmt.webapp.controllers;

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

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;

@Controller
@RequestMapping("/clients")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceRepository invoiceRepo;
    private final ClientRepository clientRepo;

    @PostMapping("/details")
    public String addInvoice(@RequestParam int id, @RequestParam MultipartFile file) {

        try {

            if (file == null) {
                return "redirect:/clients/details?id=" + id;
            }

            var client = clientRepo.findById(id).orElse(null);
            if (client == null) {
                return "redirect:/clients";
            }


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


        } catch (Exception ex) {
            System.out.println("Exception: " + ex.getMessage());
        }

        return "redirect:/clients/details?id=" + id;
    }

    @GetMapping("/invoices")
    public ResponseEntity<Object> getInvoice(@RequestParam int invoiceId) {

        try {
            Invoice invoice = invoiceRepo.findById(invoiceId).get();

            File file = new File("storage/invoices/" + invoice.getStorageFileName());
            InputStreamResource resource = new InputStreamResource(
                    new java.io.FileInputStream(file));

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + invoice.getFileName() + "\"")
                    .contentLength(file.length())
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        }
        catch(Exception ex) {
        }



        return ResponseEntity.notFound().build();
    }


    @GetMapping("/invoices/delete")
    public String deleteInvoice(@RequestParam int clientId, @RequestParam int invoiceId) {

        try {
            Invoice invoice = invoiceRepo.findById(invoiceId).get();

            Path filePath = Paths.get("storage/invoices/" + invoice.getStorageFileName());
            Files.delete(filePath);


            invoiceRepo.delete(invoice);
        }
        catch(Exception ex) {
        }

        return "redirect:/clients/details?id=" + clientId;
    }
}
