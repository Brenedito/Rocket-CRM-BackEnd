package com.rocket.crm.controllers;

import com.rocket.crm.dtos.CaktoWebhookDTO;
import com.rocket.crm.dtos.RegistroDTO;
import com.rocket.crm.services.EmpresaService;
import com.rocket.crm.services.RegistroService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
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
            @RequestHeader("x-cakto-signature") String signature,
            @RequestBody CaktoWebhookDTO payload
    ) {

        if (!webhookSecret.equals(signature)) return ResponseEntity.status(403).build();


        if ("payment.approved".equals(payload.event())) {


            RegistroDTO novoRegistro = new RegistroDTO(
                    payload.nome(),
                    payload.email(),
                    "SenhaPadrao123!", // Enviar senha por email (Manter assim por enquanto)
                    payload.nome() + " Enterprise",
                    payload.documento()
            );


            registroService.registrarNovaEmpresa(novoRegistro);

            empresaService.setPlanoPremium(payload.email());
        }

        return ResponseEntity.ok().build();
    }
}