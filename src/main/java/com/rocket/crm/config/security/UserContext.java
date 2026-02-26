package com.rocket.crm.config.security;

import com.rocket.crm.models.User;
import com.rocket.crm.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserContext {

    private final UserRepository userRepository;

    public User getUsuarioLogado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            return null;
        }

        // O 'sub' no JWT é o ID do usuário no Keycloak
        String keycloakId = jwt.getSubject();

        return userRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new RuntimeException("Usuário logado não encontrado no banco local"));
    }

    /**
     * Retorna o tenant_id do usuário logado
     * Útil para filtrar dados por tenant sem precisar carregar a entidade User completa
     */
    public UUID getTenantId() {
        User usuario = getUsuarioLogado();
        return usuario != null ? usuario.getTenant_id() : null;
    }
}