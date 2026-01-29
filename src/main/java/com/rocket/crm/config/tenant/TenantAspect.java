package com.rocket.crm.config.tenant;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.hibernate.Session;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Aspect
@Component
public class TenantAspect {

    @PersistenceContext
    private EntityManager entityManager;


    @Before("execution(* com.rocket.crm.repositories.*.*(..))")
    public void activateTenantFilter() {
        UUID tenantId = TenantContext.getCurrentTenant();

        // Só ativa se houver um tenant no contexto (evita erro no registro público)
        if (tenantId != null) {
            Session session = entityManager.unwrap(Session.class);
            session.enableFilter("tenantFilter").setParameter("tenantId", tenantId);
        }
    }
}