package com.rocket.crm.models;

import jakarta.persistence.*;
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
public class Empresa {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID empresa_id;

    @Column(nullable = false)
    private String empresa_name;

    @Column(unique = true, nullable = false)
    private String empresa_CNPJ;

    private String empresa_status;
    private String empresa_plano;
    private String cakto_id;

    @CreationTimestamp
    private LocalDateTime empresa_createdAt;
}
