package com.rocket.crm.controllers;

import com.rocket.crm.dtos.CaktoWebhookDTO;
import com.rocket.crm.dtos.RegistroDTO;
import com.rocket.crm.services.EmpresaService;
import com.rocket.crm.services.RegistroService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/webhooks/cakto")
@RequiredArgsConstructor
public class CaktoWebhookController {

    private final RegistroService registroService;
    private final EmpresaService empresaService;

    @Value("${cakto.webhook-secret}")
    private String webhookSecret;

    @PostMapping
    public ResponseEntity<Void> handleCaktoWebhook(
            @RequestHeader(value = "x-cakto-signature", required = false) String signature,
            @RequestBody CaktoWebhookDTO payload
    ) {

        if ("purchase_approved".equals(payload.event())) {
            var cliente = payload.data().customer();

            RegistroDTO novoRegistro = new RegistroDTO(
                    payload.data().customer().nome() + " Enterprise",
                    payload.data().customer().documento(),
                    payload.data().customer().nome(),
                    payload.data().customer().email(),
                    "SenhaPadrao123!" //Alterar para uma geração de senha aleatória, e implementar a alteração de senha
            );

            registroService.registrarNovaEmpresa(novoRegistro);
            empresaService.setPlanoPremium(cliente.email()); //Manter por enquanto, alterações serão pensadas no futuro
        }

        return ResponseEntity.ok().build();
    }
}