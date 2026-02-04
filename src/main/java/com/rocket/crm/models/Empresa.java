package com.rocket.crm.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "Empresas_Table")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Empresa {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "empresa_id", updatable = false, nullable = false)
    private UUID empresa_id;

    @Column(name = "empresa_name", nullable = false)
    private String empresa_name;

    @Column(name = "empresa_documento", unique = true, nullable = false)
    private String empresa_documento;

    @Column(name = "empresa_status")
    private String empresa_status;

    @Column(name = "empresa_plano")
    private String empresa_plano = "FREE";

    @Column(name = "cakto_id")
    private String cakto_id;

    @CreationTimestamp
    @Column(name = "empresa_created_at", updatable = false)
    private LocalDateTime empresa_createdAt;

    @Column(name = "data_expiracao")
    private LocalDateTime dataExpiracao;
}
