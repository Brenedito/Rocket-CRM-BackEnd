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

    @Transactional
    public void atualizarAssinatura(String email, int dias, String novoCaktoId) {
        Empresa empresa = empresaRepository.findByEmailUsuario(email)
                .orElseThrow(() -> new EmpresaNotFoundException("Empresa não encontrada para o e-mail: " + email));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsuarioNotFoundException("Usuário não encontrado"));

        // Define a base: se já expirou, usa agora; se não, soma no prazo restante
        LocalDateTime base = (empresa.getDataExpiracao() == null || empresa.getDataExpiracao().isBefore(LocalDateTime.now()))
                ? LocalDateTime.now() : empresa.getDataExpiracao();

        empresa.setDataExpiracao(base.plusDays(dias));
        empresa.setEmpresa_plano("PREMIUM");
        empresa.setEmpresa_status("ATIVO");
        keycloakService.adicionarRole(user.getKeycloakId(),"ROLE_PREMIUM");
        keycloakService.adicionarRole(user.getKeycloakId(),"ROLE_GERENTE");
        empresa.setCakto_id(novoCaktoId);

        empresaRepository.save(empresa);
    }

    @Transactional
    public void removerAcessoPremium(String email) {
        Empresa empresa = empresaRepository.findByEmailUsuario(email)
                .orElseThrow(() -> new EmpresaNotFoundException("Empresa não encontrada"));

        empresa.setEmpresa_plano("FREE");
        empresa.setEmpresa_status("INATIVO");
        empresa.setDataExpiracao(LocalDateTime.now().minusDays(1));
        empresaRepository.save(empresa);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsuarioNotFoundException("Usuário não encontrado"));

        keycloakService.removerRole(user.getKeycloakId(), "ROLE_PREMIUM");
        keycloakService.removerRole(user.getKeycloakId(), "ROLE_GERENTE");
    }
}