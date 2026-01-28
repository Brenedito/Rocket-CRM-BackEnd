package com.rocket.crm.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "Leads_Table")
@Getter @Setter @NoArgsConstructor
@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "tenantId", type = UUID.class))
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
public class Lead extends BaseEntity{

    @Column(nullable = false)
    private String lead_name;

    private String lead_email;
    private String lead_phone;
    private String lead_position;

    @Column(precision = 15, scale = 2)
    private BigDecimal lead_value;

    private String lead_origin;
    private String lead_desc;

    @CreationTimestamp
    private LocalDateTime lead_createAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_responsible_id")
    private User responsible;
}