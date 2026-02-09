package com.rocket.crm.controllers;

import com.rocket.crm.config.security.UserContext;
import com.rocket.crm.dtos.ColaboradorDTO;
import com.rocket.crm.models.User;
import com.rocket.crm.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserContext userContext;

    @PostMapping("/colaboradores")
    @PreAuthorize("hasRole('ROLE_GERENTE')") // Só o dono da conta cria time
    public ResponseEntity<Void> cadastrarColaborador(@Valid @RequestBody ColaboradorDTO dto) {
        User gerente = userContext.getUsuarioLogado();
        userService.criarNovoColaborador(dto, gerente);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/time")
    public ResponseEntity<List<User>> listarTime() {
        User usuario = userContext.getUsuarioLogado();
        return ResponseEntity.ok(userService.listarMembrosDoTime(usuario.getTenant_id()));
    }

    @PutMapping("/colaboradores/{id}")
    @PreAuthorize("hasRole('ROLE_GERENTE')")
    public ResponseEntity<Void> atualizarColaborador(@PathVariable UUID id, @Valid @RequestBody ColaboradorDTO dto) {
        User gerente = userContext.getUsuarioLogado();
        userService.atualizarColaborador(id, dto, gerente);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/colaboradores/{id}")
    @PreAuthorize("hasRole('ROLE_GERENTE')")
    public ResponseEntity<Void> removerColaborador(@PathVariable UUID id) {
        User gerente = userContext.getUsuarioLogado();
        userService.deletarColaborador(id, gerente);
        return ResponseEntity.noContent().build();
    }
}