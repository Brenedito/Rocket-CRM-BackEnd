package com.rocket.crm.controllers;

import com.rocket.crm.config.security.UserContext;
import com.rocket.crm.dtos.ColaboradorDTO;
import com.rocket.crm.models.User;
import com.rocket.crm.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserContext userContext;

    @PostMapping("/colaboradores")
    @PreAuthorize("hasRole('ROLE_GERENTE')")
    public ResponseEntity<Void> cadastrarColaborador(@RequestBody ColaboradorDTO dto) {
        // Usa o usuário local obtido a partir do token
        User gerente = userContext.getUsuarioLogado();
        userService.criarNovoColaborador(dto, gerente);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/time")
    public ResponseEntity<List<User>> listarTime() {
        User usuarioLogado = userContext.getUsuarioLogado();
        List<User> time = userService.listarMembrosDoTime(usuarioLogado.getTenant_id());
        return ResponseEntity.ok(time);
    }
}