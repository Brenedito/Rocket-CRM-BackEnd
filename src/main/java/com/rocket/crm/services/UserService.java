package com.rocket.crm.services;

import com.rocket.crm.dtos.ColaboradorDTO;
import com.rocket.crm.models.User;
import com.rocket.crm.repositories.UserRepository;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final KeycloakService keycloakService;
    private final Keycloak keycloak;

    @Value("${keycloak.realm}")
    private String realm;


    @Transactional
    public void criarNovoColaborador(ColaboradorDTO dados, User gerenteLogado) {

        UUID tenantId = gerenteLogado.getTenant_id();

        String keycloakId = provisionarUsuarioKeycloak(
                dados.email(),
                dados.nome(),
                dados.senha()
        );


        keycloakService.adicionarRole(keycloakId, "ROLE_COLABORADOR");
        keycloakService.adicionarRole(keycloakId, "ROLE_PREMIUM");


        User novoUser = new User();
        novoUser.setEmail(dados.email().trim().toLowerCase());
        novoUser.setKeycloakId(keycloakId);
        novoUser.setTenant_id(tenantId);

        userRepository.save(novoUser);
    }

    private String provisionarUsuarioKeycloak(String email, String nome, String senha) {
        UserRepresentation user = new UserRepresentation();
        user.setEnabled(true);
        user.setUsername(email.trim().toLowerCase());
        user.setEmail(email.trim().toLowerCase());
        user.setFirstName(nome);
        user.setEmailVerified(true);

        // Define a credencial com a senha vinda do DTO
        CredentialRepresentation cred = new CredentialRepresentation();
        cred.setType(CredentialRepresentation.PASSWORD);
        cred.setValue(senha);
        cred.setTemporary(false);
        user.setCredentials(Collections.singletonList(cred));

        Response response = keycloak.realm(realm).users().create(user);

        if (response.getStatus() != 201) {
            throw new RuntimeException("Erro Keycloak: " + response.getStatus());
        }

        return CreatedResponseUtil.getCreatedId(response);
    }

    public List<User> listarMembrosDoTime(UUID tenantId) {
        return userRepository.findAllByTenantId(tenantId);
    }
}