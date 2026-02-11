package com.rocket.crm.dtos;

import java.math.BigDecimal;

// DTO for partial updates: all fields nullable and no validation annotations
public record LeadUpdateDTO(
        String lead_name,
        String lead_email,
        String lead_phone,
        String lead_position,
        BigDecimal lead_value,
        String lead_origin,
        String lead_desc,
        String responsibleId,
        String lead_status
) {}

