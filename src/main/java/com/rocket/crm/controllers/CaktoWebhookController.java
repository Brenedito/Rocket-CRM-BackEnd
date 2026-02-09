package com.rocket.crm.controllers;

import com.rocket.crm.dtos.CaktoWebhookDTO;
import com.rocket.crm.services.WebhookService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/webhooks/cakto")
@RequiredArgsConstructor
public class CaktoWebhookController {

    private final WebhookService webhookService;

    @Value("${cakto.webhook-secret}")
    private String webhookSecret;

    @PostMapping
    public ResponseEntity<Void> handleCaktoWebhook(
            @RequestHeader(value = "x-cakto-signature", required = false) String signature,
            @RequestBody CaktoWebhookDTO payload
    ) {
        webhookService.processarWebhookCakto(payload);
        return ResponseEntity.ok().build();
    }
}