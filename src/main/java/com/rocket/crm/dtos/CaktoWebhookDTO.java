package com.rocket.crm.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CaktoWebhookDTO(
        String event,
        CaktoDataDTO data
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CaktoDataDTO(
            String id,
            CaktoCustomerDTO customer
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CaktoCustomerDTO(
            @JsonProperty("name") String nome,
            @JsonProperty("email") String email,
            @JsonProperty("docNumber") String documento
    ) {}
}