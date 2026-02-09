package com.rocket.crm.services;

import com.rocket.crm.dtos.CaktoWebhookDTO;
import com.rocket.crm.dtos.RegistroDTO;
import com.rocket.crm.models.Empresa;
import com.rocket.crm.models.User;
import com.rocket.crm.repositories.EmpresaRepository;
import com.rocket.crm.repositories.UserRepository;
import com.rocket.crm.utils.PasswordGenerator;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WebhookService {

    private final EmpresaRepository empresaRepository;
    private final UserRepository usuarioRepository;
    private final Keycloak keycloak;
    private final EmpresaService empresaService;
    private final EmailService emailService;
    private final PasswordGenerator passwordGenerator;
    private final KeycloakService keycloakService;

    @Value("${keycloak.realm}")
    private String realm;

    @Transactional
    public void processarWebhookCakto(CaktoWebhookDTO payload) {
        String evento = payload.event();
        var cliente = payload.data().customer();

        switch (evento) {
            case "purchase_approved" -> {
                String senhaAleatoria = PasswordGenerator.generate(8);
                RegistroDTO novoRegistro = new RegistroDTO(
                        cliente.nome() + " Enterprise",
                        cliente.documento(),
                        cliente.nome(),
                        cliente.email(),
                        senhaAleatoria
                );

                registrarNovaEmpresa(novoRegistro, payload);

                try {
                    emailService.enviarCredenciais(cliente.email(), cliente.nome(), senhaAleatoria);
                } catch (Exception e) {
                    System.err.println("Erro ao enviar e-mail: " + e.getMessage());
                }
            }

            case "subscription_renewed" -> {
                // Estende o prazo por mais 30 dias e atualiza o ID da transação
                empresaService.atualizarAssinatura(cliente.email(), 30, payload.data().id());
                System.out.println("LOG: Assinatura renovada para " + cliente.email());
            }

            case "subscription_canceled" -> {
                // Remove o acesso Premium imediatamente no DB e Keycloak
                empresaService.removerAcessoPremium(cliente.email());
                System.out.println("LOG: Assinatura cancelada para " + cliente.email());
            }

            case "refund" -> {
                // Bloqueio imediato por estorno
                empresaService.removerAcessoPremium(cliente.email());
                System.out.println("LOG: reebolso de assinatura para " + cliente.email());
            }
        }
    }


    @Transactional
    public void registrarNovaEmpresa(RegistroDTO dados, CaktoWebhookDTO payload) {
        String keycloakUserId = criarUsuarioNoKeycloak(dados);

        // Criamos a empresa já com os dados do webhook
        Empresa empresa = criarEmpresa(dados, payload);

        criarUsuarioLocal(dados, keycloakUserId, empresa.getEmpresa_id());

        // Atribui as roles logo após a criação
        keycloakService.adicionarRole(keycloakUserId, "ROLE_GERENTE");
        keycloakService.adicionarRole(keycloakUserId, "ROLE_PREMIUM");
    }

    private Empresa criarEmpresa(RegistroDTO dados, CaktoWebhookDTO payload) {
        Empresa empresa = new Empresa();
        empresa.setEmpresa_name(dados.nomeEmpresa());
        empresa.setEmpresa_documento(dados.documento());
        empresa.setEmpresa_status("ATIVO");
        empresa.setEmpresa_plano("PREMIUM");

        if (payload != null && payload.data() != null) {
            empresa.setCakto_id(payload.data().id());
            empresa.setDataExpiracao(LocalDateTime.now().plusDays(30));
        }

        return empresaRepository.save(empresa);
    }

    private String criarUsuarioNoKeycloak(RegistroDTO dados) {
        UserRepresentation user = new UserRepresentation();
        user.setEnabled(true);
        user.setUsername(dados.email().trim().toLowerCase());
        user.setEmail(dados.email().trim().toLowerCase());
        user.setFirstName(dados.nomeAdmin());
        user.setEmailVerified(true);
        user.setRequiredActions(Collections.emptyList());
        user.setCredentials(Collections.singletonList(criarCredenciais(dados.senha())));

        Response response = keycloak.realm(realm).users().create(user);

        if (response.getStatus() != 201) {
            throw new RuntimeException("Erro ao criar usuário no Keycloak. Status: " + response.getStatus());
        }

        return CreatedResponseUtil.getCreatedId(response);
    }

    private CredentialRepresentation criarCredenciais(String senha) {

        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(senha);
        credential.setTemporary(false);
        return credential;
    }

    private Empresa criarEmpresa(RegistroDTO dados) {

        Empresa empresa = new Empresa();
        empresa.setEmpresa_name(dados.nomeEmpresa());
        empresa.setEmpresa_documento(dados.documento());
        empresa.setEmpresa_status("ATIVO");
        empresa.setEmpresa_plano("FREE");
        return empresaRepository.save(empresa);
    }

    private void criarUsuarioLocal(RegistroDTO dados, String keycloakId, UUID tenantId) {

        User usuario = new User();
        usuario.setEmail(dados.email());
        usuario.setKeycloakId(keycloakId);
        usuario.setTenant_id(tenantId);
        usuarioRepository.save(usuario);
    }
}

