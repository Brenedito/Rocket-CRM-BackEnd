package com.rocket.crm.services;

import com.rocket.crm.config.tenant.TenantContext;
import com.rocket.crm.dtos.LeadRequestDTO;
import com.rocket.crm.dtos.LeadUpdateDTO;
import com.rocket.crm.models.Lead;
import com.rocket.crm.enums.LeadStatus;
import com.rocket.crm.repositories.LeadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LeadService {

    private final LeadRepository leadRepository;

    @Transactional
    public Lead criarNovoLead(LeadRequestDTO dto) {
        UUID tenantId = TenantContext.getCurrentTenant();

        if (tenantId == null) {
            throw new RuntimeException("ERRO CRÍTICO: TenantID não encontrado no contexto!");
        }

        Lead lead = new Lead();
        lead.setLead_name(dto.lead_name());
        lead.setLead_email(dto.lead_email());
        lead.setLead_phone(dto.lead_phone());
        lead.setLead_position(dto.lead_position());
        lead.setLead_value(dto.lead_value());
        lead.setLead_origin(dto.lead_origin());
        lead.setLead_desc(dto.lead_desc());

        // Set status from DTO if provided, otherwise default to NEW
        if (dto.lead_status() != null && !dto.lead_status().isBlank()) {
            try {
                lead.setLead_status(LeadStatus.valueOf(dto.lead_status().trim().toUpperCase()));
            } catch (IllegalArgumentException ex) {
                // invalid status provided; keep default NEW
                lead.setLead_status(LeadStatus.NOVO);
            }
        }

        lead.setTenant_id(TenantContext.getCurrentTenant());

        return leadRepository.save(lead);
    }

    @Transactional
    public Lead atualizarLead(UUID id, LeadUpdateDTO dto) {
        Lead lead = leadRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lead não encontrado"));

        // Only update fields that are present (non-null) in the incoming DTO
        if (dto.lead_name() != null) lead.setLead_name(dto.lead_name());
        if (dto.lead_email() != null) lead.setLead_email(dto.lead_email());
        if (dto.lead_phone() != null) lead.setLead_phone(dto.lead_phone());
        if (dto.lead_position() != null) lead.setLead_position(dto.lead_position());
        if (dto.lead_value() != null) lead.setLead_value(dto.lead_value());
        if (dto.lead_origin() != null) lead.setLead_origin(dto.lead_origin());
        if (dto.lead_desc() != null) lead.setLead_desc(dto.lead_desc());

        if (dto.lead_status() != null && !dto.lead_status().isBlank()) {
            try {
                lead.setLead_status(LeadStatus.valueOf(dto.lead_status().trim().toUpperCase()));
            } catch (IllegalArgumentException ex) {
                // ignore invalid status
            }
        }

        return leadRepository.save(lead);
    }

    @Transactional
    public void deletarLead(UUID id) {
        // Como usamos o Hibernate Filter (tenantFilter), o findById já está protegido
        Lead lead = leadRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lead não encontrado ou acesso negado"));
        leadRepository.delete(lead);
    }



    @Transactional(readOnly = true)
    public List<Lead> listarLeadsDaEmpresa() {
        return leadRepository.findAll();
    }
}