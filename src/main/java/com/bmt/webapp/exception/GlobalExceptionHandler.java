package com.bmt.webapp.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;


@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ClientNotFoundException.class)
    public String handleClientNotFound(ClientNotFoundException ex) {
        log.warn("Client not found: {}", ex.getMessage());
        return "redirect:/clients";
    }

    @ExceptionHandler(InvoiceNotFoundException.class)
    public String handleClientNotFound(InvoiceNotFoundException ex) {
        log.warn("Invoice not found: {}", ex.getMessage());
        return "redirect:/clients";
    }

    @ExceptionHandler(Exception.class)
    public String handleAllExceptions(Exception ex, Model model) {
        log.error("Unexpected error occurred: ", ex);
        model.addAttribute("errorMessage", "An unexpected error occurred. Please try again later");
        return "clients/general-error";
    }
}
