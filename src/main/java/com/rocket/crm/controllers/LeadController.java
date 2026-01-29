package com.rocket.crm.controllers;

import com.rocket.crm.dtos.LeadRequestDTO;
import com.rocket.crm.models.Lead;
import com.rocket.crm.services.LeadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leads")
@RequiredArgsConstructor
public class LeadController {

    private final LeadService leadService;

    @PostMapping
    public ResponseEntity<Lead> criar(@Valid @RequestBody LeadRequestDTO dto) {
        Lead novoLead = leadService.criarNovoLead(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(novoLead);
    }

    @GetMapping
    public ResponseEntity<List<Lead>> listar() {
        return ResponseEntity.ok(leadService.listarLeadsDaEmpresa());
    }
}