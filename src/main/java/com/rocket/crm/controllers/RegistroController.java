package com.rocket.crm.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/registro")
@RequiredArgsConstructor
public class RegistroController {

//    private final RegistroService registroService;
//
//    @PostMapping
//    public ResponseEntity<String> registrar(@Valid @RequestBody RegistroDTO dados) {
//        registroService.registrarNovaEmpresa(dados);
//        return ResponseEntity.status(HttpStatus.CREATED).body("Empresa e Administrador registrados com sucesso!");
//    }
}