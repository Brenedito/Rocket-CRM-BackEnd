package com.rocket.crm.services;

import com.rocket.crm.models.Empresa;
import com.rocket.crm.models.User;
import com.rocket.crm.repositories.EmpresaRepository;
import com.rocket.crm.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmpresaService {

    private final EmpresaRepository empresaRepository;
    private final UserRepository userRepository;

    @Transactional
    public void setPlanoPremium(String emailUsuario) {
        // 1. Busca o usuário pelo e-mail (que veio da Cakto)
        User usuario = userRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado para upgrade"));

        // 2. Busca a empresa vinculada ao tenant_id do usuário
        Empresa empresa = empresaRepository.findById(usuario.getTenant_Id())
                .orElseThrow(() -> new RuntimeException("Empresa não encontrada para este usuário"));

        // 3. Faz o upgrade
        empresa.setEmpresa_plano("PREMIUM");
        empresaRepository.save(empresa);

    }
}