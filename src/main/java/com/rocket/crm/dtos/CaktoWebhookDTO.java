package com.rocket.crm.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CaktoWebhookDTO(
        String event,
        @JsonProperty("customer_name") String nome,
        @JsonProperty("customer_email") String email,
        @JsonProperty("customer_document") String documento, // CPF ou CNPJ que validamos antes
        @JsonProperty("payment_status") String status
) {}
