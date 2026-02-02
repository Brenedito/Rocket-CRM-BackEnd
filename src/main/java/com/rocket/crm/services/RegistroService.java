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

import java.util.Collections;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RegistroService {

    private final EmpresaRepository empresaRepository;
    private final UserRepository usuarioRepository;
    private final Keycloak keycloak;
    private final EmpresaService empresaService;
    private final EmailService emailService;
    private final PasswordGenerator passwordGenerator;

    @Value("${keycloak.realm}")
    private String realm;

    @Transactional
    public void processarWebhookCakto(CaktoWebhookDTO payload) {
        if ("purchase_approved".equals(payload.event())) {
            var cliente = payload.data().customer();

            String senhaAleatoria = PasswordGenerator.generate(8);

            RegistroDTO novoRegistro = new RegistroDTO(
                    cliente.nome() + " Enterprise",
                    cliente.documento(),
                    cliente.nome(),
                    cliente.email(),
                    senhaAleatoria // implementar a alteração de senha
            );


            emailService.enviarCredenciais(cliente.email(), cliente.nome(), senhaAleatoria); // Testar com a aplicação Real

            registrarNovaEmpresa(novoRegistro);
            empresaService.setPlanoPremium(cliente.email()); //Manter por enquanto, alterações serão pensadas no futuro
        }
    }

    @Transactional
    public void registrarNovaEmpresa(RegistroDTO dados) {

        String keycloakUserId = criarUsuarioNoKeycloak(dados);

        Empresa empresa = criarEmpresa(dados);

        criarUsuarioLocal(dados, keycloakUserId, empresa.getEmpresa_id());
    }

    private String criarUsuarioNoKeycloak(RegistroDTO dados) {
        UserRepresentation user = new UserRepresentation();
        user.setEnabled(true);
        user.setUsername(dados.email().trim().toLowerCase());
        user.setEmail(dados.email().trim().toLowerCase());
        user.setFirstName(dados.nomeAdmin());
        user.setEmailVerified(true);
        user.setRequiredActions(Collections.emptyList());
        user.setCredentials(Collections.singletonList(criarSenha(dados.senha())));

        Response response = keycloak.realm(realm).users().create(user);

        if (response.getStatus() != 201) {
            throw new RuntimeException("Erro ao criar usuário no Keycloak. Status: " + response.getStatus());
        }

        return CreatedResponseUtil.getCreatedId(response);
    }

    private CredentialRepresentation criarSenha(String senha) {

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
        usuario.setTenant_Id(tenantId);
        usuarioRepository.save(usuario);
    }
}
