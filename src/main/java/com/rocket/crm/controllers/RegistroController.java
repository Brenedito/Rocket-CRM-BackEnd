package com.rocket.crm.controllers;

import com.rocket.crm.dtos.RegistroDTO;
import com.rocket.crm.services.RegistroService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/registro")
@RequiredArgsConstructor
public class RegistroController {

    private final RegistroService registroService;

    @PostMapping
    public ResponseEntity<String> registrar(@Valid @RequestBody RegistroDTO dados) {
        registroService.registrarNovaEmpresa(dados);
        return ResponseEntity.status(HttpStatus.CREATED).body("Empresa e Administrador registrados com sucesso!");
    }
}