package com.rocket.crm.schedulers;

import com.rocket.crm.models.Empresa;
import com.rocket.crm.models.User;
import com.rocket.crm.repositories.EmpresaRepository;
import com.rocket.crm.repositories.UserRepository;
import com.rocket.crm.services.KeycloakService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AssinaturaScheduler {

    private final EmpresaRepository empresaRepository;
    private final KeycloakService keycloakService;
    private final UserRepository userRepository;

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void verificarAssinaturasExpiradas() {
        System.out.println("LOG: Iniciando varredura de assinaturas expiradas...");

        // Certifique-se que o Repo usa o nome do campo Java: empresa_plano e dataExpiracao
        List<Empresa> expiradas = empresaRepository.findAllByPlanoAndDataExpiracaoBefore(
                "PREMIUM", LocalDateTime.now()
        );

        for (Empresa empresa : expiradas) {
            try {
                System.out.println("Bloqueando empresa: " + empresa.getEmpresa_name()); // getEmpresa_name
                empresa.setEmpresa_plano("FREE"); // setEmpresa_plano
                empresaRepository.save(empresa);

                List<User> usuariosParaBloquear = userRepository.findAllByTenantId(empresa.getEmpresa_id()); // getEmpresa_id
                for (User user : usuariosParaBloquear) {
                    try {
                        keycloakService.removerRole(user.getKeycloakId(), "ROLE_PREMIUM"); // getKeycloakId
                    } catch (Exception e) {
                        System.err.println("Erro para user: " + user.getEmail()); // getEmail
                    }
                }
            } catch (Exception e) {
                System.err.println("Erro na empresa: " + empresa.getEmpresa_name());
            }
        }
    }
}