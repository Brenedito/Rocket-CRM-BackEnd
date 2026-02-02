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

        User usuario = userRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado para upgrade"));


        Empresa empresa = empresaRepository.findById(usuario.getTenant_Id())
                .orElseThrow(() -> new RuntimeException("Empresa não encontrada para este usuário"));


        empresa.setEmpresa_plano("PREMIUM");
        empresaRepository.save(empresa);

    }
}