package com.rocket.crm.services;

import com.rocket.crm.dtos.RegistroDTO;
import com.rocket.crm.models.Empresa;
import com.rocket.crm.models.User;
import com.rocket.crm.repositories.EmpresaRepository;
import com.rocket.crm.repositories.UserRepository;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;

import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;

import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RegistroService {

    private final EmpresaRepository empresaRepository;
    private final UserRepository usuarioRepository;
    private final Keycloak keycloak;

    @Value("${keycloak.realm}")
    private String realm;

    @Transactional
    public void registrarNovaEmpresa(RegistroDTO dados) {

        // 1. Salvar a Empresa (Gera o Tenant ID)

        Empresa novaEmpresa = new Empresa();
        novaEmpresa.setEmpresa_name(dados.nomeEmpresa());
        novaEmpresa.setEmpresa_CNPJ(dados.cnpj());
        novaEmpresa.setEmpresa_status("ATIVO");
        novaEmpresa.setEmpresa_plano("FREE");
        empresaRepository.save(novaEmpresa);

        // 2. Criar Usuário no Keycloak

        UserRepresentation user = new UserRepresentation();
        user.setEnabled(true);
        user.setUsername(dados.email());
        user.setEmail(dados.email());
        user.setFirstName(dados.nomeAdmin());

        Response response = keycloak.realm(realm).users().create(user);

        if (response.getStatus() == 201) {
            String keycloakId = CreatedResponseUtil.getCreatedId(response);

            // 3. Salvar Usuário no Banco Local vinculado ao Tenant

            User usuarioLocal = new User();
            usuarioLocal.setEmail(dados.email());
            usuarioLocal.setKeycloakId(keycloakId);
            usuarioLocal.setTenant_Id(novaEmpresa.getEmpresa_id());
            usuarioRepository.save(usuarioLocal);

            // 4. Definir Senha e Roles (Opcional aqui ou via convite por e-mail)
        } else {

            String errorBody = response.readEntity(String.class);
            System.out.println("Erro do Keycloak: " + response.getStatus() + " - " + errorBody);
            throw new RuntimeException("Falha ao criar usuário: " + response.getStatusInfo());
        }
    }
}
