package com.rocket.crm.services;

import com.rocket.crm.exceptions.EmpresaNotFoundException;
import com.rocket.crm.exceptions.UsuarioNotFoundException;
import com.rocket.crm.models.Empresa;
import com.rocket.crm.models.User;
import com.rocket.crm.repositories.EmpresaRepository;
import com.rocket.crm.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class EmpresaService {

    private final EmpresaRepository empresaRepository;
    private final UserRepository userRepository;
    private final KeycloakService keycloakService;


    @Transactional
    public void setPlanoPremium(String emailUsuario) {

        User usuario = userRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new UsuarioNotFoundException("Usuário não encontrado para upgrade"));


        Empresa empresa = empresaRepository.findById(usuario.getTenant_id())
                .orElseThrow(() -> new EmpresaNotFoundException("Empresa não encontrada para este usuário"));


        empresa.setEmpresa_plano("PREMIUM");
        empresaRepository.save(empresa);

    }

    public void atualizarAssinatura(String email, int dias) {
        Empresa empresa = empresaRepository.findByEmailUsuario(email)
                .orElseThrow(() -> new EmpresaNotFoundException("Empresa não encontrada para o e-mail: " + email));

        // Se a empresa já expirou, conta a partir de hoje. Se não, soma na data atual.
        LocalDateTime base = (empresa.getDataExpiracao() == null || empresa.getDataExpiracao().isBefore(LocalDateTime.now())) ? LocalDateTime.now() : empresa.getDataExpiracao();

        empresa.setDataExpiracao(base.plusDays(dias));
        empresa.setEmpresa_plano("PREMIUM");
        empresaRepository.save(empresa);
    }

    public void removerAcessoPremium(String email) {
        Empresa empresa = empresaRepository.findByEmailUsuario(email)
                .orElseThrow(() -> new EmpresaNotFoundException("Empresa não encontrada para o e-mail: " + email));
        empresa.setEmpresa_plano("FREE");
        empresaRepository.save(empresa);


        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsuarioNotFoundException("Usuário não encontrado"));

        keycloakService.removerRole(user.getKeycloakId(), "ROLE_PREMIUM");
    }
}