package com.rocket.crm.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

public record LeadRequestDTO(
        @NotBlank(message = "O nome do lead é obrigatório")
        String lead_name,

        @Email(message = "E-mail com formato inválido")
        String lead_email,

        String lead_phone,
        String lead_position,
        BigDecimal lead_value,
        String lead_origin,
        String lead_desc,
        String responsibleId
) {}