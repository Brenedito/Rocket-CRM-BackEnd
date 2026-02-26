package com.rocket.crm.dtos;

import com.rocket.crm.enums.LeadStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetricsDTO {

    /**
     * Total de leads criados em toda a história da empresa
     */
    private long totalLeadsGeral;

    /**
     * Total de leads criados dentro do período filtrado
     */
    private long totalLeadsPeriodo;

    /**
     * Percentual de leads ganhos em relação ao total do período (0-100)
     * Evita divisão por zero
     */
    private double taxaConversao;

    /**
     * Soma de todos os valores dos leads no período
     */
    private BigDecimal receitaPotencial;

    /**
     * Soma dos valores apenas dos leads com status GANHO no período
     */
    private BigDecimal ganhoNoPeriodo;

    /**
     * Mapa contendo a contagem de leads agrupada por cada status
     */
    private Map<LeadStatus, Long> leadsPorStatus;
}
