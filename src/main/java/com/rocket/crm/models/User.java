package com.rocket.crm.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "Users_Table")
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID user_id;

    @Column(unique = true, nullable = false)
    private String user_email;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_tenant_id", nullable = false)
    private Empresa empresa;
}