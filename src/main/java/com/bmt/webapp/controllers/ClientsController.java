package com.bmt.webapp.controllers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.NoSuchElementException;

import com.bmt.webapp.mappers.ClientMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import com.bmt.webapp.models.Client;
import com.bmt.webapp.models.ClientDto;
import com.bmt.webapp.repositories.ClientRepository;


import jakarta.validation.Valid;

@Controller
@RequestMapping("/clients")
@RequiredArgsConstructor
public class ClientsController {

    private final ClientRepository clientRepo;
    private final ClientMapper clientMapper;

    @GetMapping({"", "/"})
    public String getClients(Model model,
                             @RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "10") int size,
                             @RequestParam(defaultValue = "id") String sortBy,
                             @RequestParam(defaultValue = "desc") String direction
    ) {
        Sort sort = direction.equalsIgnoreCase("asc") ?
                Sort.by(sortBy).ascending() :
                Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Client> clientPage = clientRepo.findAll(pageable);

        model.addAttribute("clients", clientPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", clientPage.getTotalPages());
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("direction", direction);

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

        Client client = clientMapper.mapToClient(clientDto);
        clientRepo.save(client);

        return "redirect:/clients";
    }


    @GetMapping("/edit")
    public String editClient(Model model, @RequestParam int id) {

        Client client = clientRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Client not found with id " + id));

        ClientDto clientDto = clientMapper.mapToClientDto(client);
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
        Client client = clientRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Client not found with id " + id));
        model.addAttribute("client", client);

        if (clientRepo.findByEmail(clientDto.getEmail()) != null) {
            result.addError(
                    new FieldError("clientDto", "email", clientDto.getEmail()
                            , false, null, null, "Email address is already used")
            );
        }

        if (result.hasErrors()) {
            return "clients/edit";
        }

        clientMapper.updateClientFromDto(clientDto, client);
        clientRepo.save(client);

        return "redirect:/clients";
    }


    @GetMapping("/delete")
    public String deleteClient(@RequestParam int id) throws IOException {

        Client client = clientRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Client not found with id " + id));

        for (var invoice : client.getInvoices()) {
            Path filePath = Paths.get("storage/invoices/" + invoice.getStorageFileName());
            Files.delete(filePath);
        }

        clientRepo.delete(client);
        return "redirect:/clients";
    }


    @GetMapping("/details")
    public String getClient(Model model, @RequestParam int id) {

        Client client = clientRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Client not found with id " + id));
        model.addAttribute("client", client);

        return "clients/details";
    }

}
