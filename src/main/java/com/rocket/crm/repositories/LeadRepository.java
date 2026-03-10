package com.rocket.crm.repositories;

import com.rocket.crm.models.Lead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Repository
public interface LeadRepository extends JpaRepository<Lead, UUID> {

    /**
     * Retorna o total de leads criados em toda a história da empresa
     */
    @Query("SELECT COUNT(l) FROM Lead l WHERE l.tenant_id = :tenantId")
    long countTotalLeads(@Param("tenantId") UUID tenantId);

    /**
     * Retorna o total de leads atualizados dentro de um período específico
     */
    @Query("SELECT COUNT(l) FROM Lead l WHERE l.tenant_id = :tenantId AND l.lead_createAt BETWEEN :startDate AND :endDate")
    long countLeadsByPeriod(@Param("tenantId") UUID tenantId,
                            @Param("startDate") LocalDateTime startDate,
                            @Param("endDate") LocalDateTime endDate);

    /**
     * Retorna a soma de todos os valores dos leads no período, exceto os perdidos
     */
    @Query("SELECT COALESCE(SUM(l.lead_value), 0) FROM Lead l WHERE l.tenant_id = :tenantId AND l.lead_createAt BETWEEN :startDate AND :endDate AND l.lead_status <> 'PERDIDO'")
    BigDecimal sumLeadValueByPeriod(@Param("tenantId") UUID tenantId,
                                    @Param("startDate") LocalDateTime startDate,
                                    @Param("endDate") LocalDateTime endDate);

    /**
     * Retorna a soma dos valores apenas dos leads com status GANHO atualizados no período
     */
    @Query("SELECT COALESCE(SUM(l.lead_value), 0) FROM Lead l WHERE l.tenant_id = :tenantId AND l.lead_status = 'GANHO' AND l.lead_lastUpdate BETWEEN :startDate AND :endDate")
    BigDecimal sumWonLeadValueByPeriod(@Param("tenantId") UUID tenantId,
                                       @Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate);

    /**
     * Retorna a contagem de leads com status GANHO atualizados no período
     */
    @Query("SELECT COUNT(l) FROM Lead l WHERE l.tenant_id = :tenantId AND l.lead_status = 'GANHO' AND l.lead_lastUpdate BETWEEN :startDate AND :endDate")
    long countWonLeadsByPeriod(@Param("tenantId") UUID tenantId,
                               @Param("startDate") LocalDateTime startDate,
                               @Param("endDate") LocalDateTime endDate);

    /**
     * Retorna o agrupamento de leads por status no período
     * Retorna pares de [status, count] com CAST para String do enum LeadStatus
     */
    @Query("SELECT new map(CAST(l.lead_status as String) as status, COUNT(l) as count) FROM Lead l WHERE l.tenant_id = :tenantId AND l.lead_createAt BETWEEN :startDate AND :endDate GROUP BY l.lead_status")
    List<Map<String, Object>> countLeadsByStatusInPeriod(@Param("tenantId") UUID tenantId,
                                                          @Param("startDate") LocalDateTime startDate,
                                                          @Param("endDate") LocalDateTime endDate);
}
