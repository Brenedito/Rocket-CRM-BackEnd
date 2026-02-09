package com.rocket.crm.services;

import com.rocket.crm.config.tenant.TenantContext;
import com.rocket.crm.dtos.LeadRequestDTO;
import com.rocket.crm.models.Lead;
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


        lead.setTenant_id(TenantContext.getCurrentTenant());

        return leadRepository.save(lead);
    }

    @Transactional
    public Lead atualizarLead(UUID id, LeadRequestDTO dto) {
        Lead lead = leadRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lead não encontrado"));

        lead.setLead_name(dto.lead_name());
        lead.setLead_email(dto.lead_email());
        lead.setLead_phone(dto.lead_phone());
        lead.setLead_position(dto.lead_position());
        lead.setLead_value(dto.lead_value());
        lead.setLead_origin(dto.lead_origin());
        lead.setLead_desc(dto.lead_desc());

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