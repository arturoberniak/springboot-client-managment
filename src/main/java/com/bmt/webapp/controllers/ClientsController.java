package com.bmt.webapp.controllers;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.bmt.webapp.models.Client;
import com.bmt.webapp.models.ClientDto;
import com.bmt.webapp.models.Invoice;
import com.bmt.webapp.repositories.ClientRepository;
import com.bmt.webapp.repositories.InvoiceRepository;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/clients")
public class ClientsController {

	@Autowired
	private ClientRepository clientRepo;
	
	
	@GetMapping({"", "/"})
	public String getClients(Model model) {
		var clients = clientRepo.findAll(Sort.by(Sort.Direction.DESC, "id"));
		model.addAttribute("clients", clients);
		
		return "clients/index";
	}
	
	
	@GetMapping("/create")
	public String createClient(Model model) {
		ClientDto clientDto = new ClientDto();
    	model.addAttribute("clientDto", clientDto);
		
		return "clients/create";
	}
	
	
	@PostMapping("/create")
    public String createClient(
            @Valid @ModelAttribute ClientDto clientDto,
            BindingResult result
            ) {
		
		if (clientRepo.findByEmail(clientDto.getEmail()) != null) {
			result.addError(
    				new FieldError("clientDto", "email", clientDto.getEmail()
    						, false, null, null, "Email address is already used")
    		);
		}
		
		if (result.hasErrors()) {
    		return "clients/create";
    	}
		
		
		Client client = new Client();
    	client.setFirstName(clientDto.getFirstName());
    	client.setLastName(clientDto.getLastName());
    	client.setEmail(clientDto.getEmail());
    	client.setPhone(clientDto.getPhone());
    	client.setAddress(clientDto.getAddress());
    	client.setStatus(clientDto.getStatus());
    	client.setCreatedAt(new Date());
    	
    	clientRepo.save(client);
		
		return "redirect:/clients";
	}
	
	
	@GetMapping("/edit")
	public String editClient(Model model, @RequestParam int id) {
		Client client = clientRepo.findById(id).orElse(null);
		if (client == null) {
			return "redirect:/clients";
		}
		
		
		ClientDto clientDto = new ClientDto();
		clientDto.setFirstName(client.getFirstName());
		clientDto.setLastName(client.getLastName());
		clientDto.setEmail(client.getEmail());
		clientDto.setPhone(client.getPhone());
		clientDto.setAddress(client.getAddress());
		clientDto.setStatus(client.getStatus());
		
		model.addAttribute("client", client);
    	model.addAttribute("clientDto", clientDto);
		
		return "clients/edit";
	}
	
	
	@PostMapping("/edit")
	public String editClient(
			Model model,
            @RequestParam int id,
            @Valid @ModelAttribute ClientDto clientDto,
            BindingResult result
            ) {
		
		
		Client client = clientRepo.findById(id).orElse(null);
		if (client == null) {
			return "redirect:/clients";
		}
		
		
		model.addAttribute("client", client);
    	
		if (result.hasErrors()) {
    		return "clients/edit";
    	}
		

		client.setFirstName(clientDto.getFirstName());
    	client.setLastName(clientDto.getLastName());
    	client.setEmail(clientDto.getEmail());
    	client.setPhone(clientDto.getPhone());
    	client.setAddress(clientDto.getAddress());
    	client.setStatus(clientDto.getStatus());
    	
    	
    	try {
    		clientRepo.save(client);
    	}
    	catch(Exception ex) {
    		result.addError(
    				new FieldError("clientDto", "email", clientDto.getEmail()
    						, false, null, null, "Email address is already used")
    		);
    		
    		return "clients/edit";
    	}
    	
		
		return "redirect:/clients";
	}
	
	
	@GetMapping("/delete")
	public String deleteClient(@RequestParam int id) {
		
		Client client = clientRepo.findById(id).orElse(null);
		
		if (client != null) {

			for(var invoice : client.getInvoices()) {
				try {
					Path filePath = 
							Paths.get("storage/invoices/" + invoice.getStorageFileName());
		    		Files.delete(filePath);
				}
				catch(Exception ex) {
				}
			}
			
			
			
			
			clientRepo.delete(client);
		}
		
		return "redirect:/clients";
	}
	
	
	@GetMapping("/details")
	public String getClient(Model model, @RequestParam int id) {
		Client client = clientRepo.findById(id).orElse(null);
		if (client == null) {
			return "redirect:/clients";
		}
		
		model.addAttribute("client", client);
		
		return "clients/details";
	}
	
	
	
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
	
	
	@Autowired
	private InvoiceRepository invoiceRepo;
	
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
