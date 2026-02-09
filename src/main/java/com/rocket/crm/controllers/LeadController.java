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
import java.util.UUID;

@RestController
@RequestMapping("/api/leads")
@RequiredArgsConstructor
public class LeadController {

    private final LeadService leadService;

    @PostMapping
    public ResponseEntity<Lead> criar(@Valid @RequestBody LeadRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(leadService.criarNovoLead(dto));
    }

    @GetMapping
    public ResponseEntity<List<Lead>> listar() {
        return ResponseEntity.ok(leadService.listarLeadsDaEmpresa());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Lead> atualizar(@PathVariable UUID id, @Valid @RequestBody LeadRequestDTO dto) {
        return ResponseEntity.ok(leadService.atualizarLead(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable UUID id) {
        leadService.deletarLead(id);
        return ResponseEntity.noContent().build();
    }
}