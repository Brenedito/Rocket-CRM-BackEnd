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

    @Value("${cakto.webhook-secret}")
    private String webhookSecret;

    @PostMapping
    public ResponseEntity<Void> handleCaktoWebhook(
            @RequestHeader(value = "x-cakto-signature", required = false) String signature,
            @RequestBody CaktoWebhookDTO payload
    ) {
        registroService.processarWebhookCakto(payload);
        return ResponseEntity.ok().build();
    }
}