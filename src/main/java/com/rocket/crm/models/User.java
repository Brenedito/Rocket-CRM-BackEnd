package com.rocket.crm.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import java.time.LocalDateTime;
import java.util.UUID;


@Entity
@Table(name = "Users_Table")
@Getter
@Setter
@NoArgsConstructor
@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "tenantId", type = UUID.class))
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
public class User extends BaseEntity{

    @Column(name = "user_email", unique = true, nullable = false)
    private String email;

    @Column(name = "keycloak_id", unique = true)
    private String keycloakId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", insertable = false, updatable = false)
    private Empresa empresa;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime created_At;
}