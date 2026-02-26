package com.rocket.crm.services;

import com.rocket.crm.dtos.MetricsDTO;
import com.rocket.crm.enums.LeadStatus;
import com.rocket.crm.repositories.LeadRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class MetricsService {

    private final LeadRepository leadRepository;

    /**
     * Calcula todas as métricas de leads para um período específico
     *
     * @param tenantId ID da empresa (tenant)
     * @param startDate Data de início do período (LocalDate)
     * @param endDate Data de fim do período (LocalDate)
     * @return MetricsDTO com todos os dados calculados
     */
    public MetricsDTO getMetricsByPeriod(UUID tenantId, LocalDate startDate, LocalDate endDate) {
        // Converter LocalDate para LocalDateTime
        // startDate: começa no início do dia (00:00:00)
        // endDate: vai até o fim do dia (23:59:59)
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        log.info("Calculando métricas para tenant: {} entre {} e {}", tenantId, startDate, endDate);
        log.debug("Intervalo de tempo convertido: {} a {}", startDateTime, endDateTime);

        // 1. Total de leads geral (toda a história)
        long totalLeadsGeral = leadRepository.countTotalLeads(tenantId);
        log.debug("Total de leads geral: {}", totalLeadsGeral);

        // 2. Total de leads no período
        long totalLeadsPeriodo = leadRepository.countLeadsByPeriod(tenantId, startDateTime, endDateTime);
        log.debug("Total de leads no período: {}", totalLeadsPeriodo);

        // 3. Soma de valores no período (receita potencial)
        BigDecimal receitaPotencial = leadRepository.sumLeadValueByPeriod(tenantId, startDateTime, endDateTime);
        if (receitaPotencial == null) {
            receitaPotencial = BigDecimal.ZERO;
        }
        log.debug("Receita potencial no período: {}", receitaPotencial);

        // 4. Contagem de leads ganhos no período
        long totalLeadsGanhos = leadRepository.countWonLeadsByPeriod(tenantId, startDateTime, endDateTime);
        log.debug("Total de leads ganhos no período: {}", totalLeadsGanhos);

        // 5. Taxa de conversão (percentual)
        double taxaConversao = calcularTaxaConversao(totalLeadsGanhos, totalLeadsPeriodo);
        log.debug("Taxa de conversão: {}%", taxaConversao);

        // 6. Ganho no período (soma dos valores dos leads ganhos)
        BigDecimal ganhoNoPeriodo = leadRepository.sumWonLeadValueByPeriod(tenantId, startDateTime, endDateTime);
        if (ganhoNoPeriodo == null) {
            ganhoNoPeriodo = BigDecimal.ZERO;
        }
        log.debug("Ganho no período: {}", ganhoNoPeriodo);

        // 7. Leads agrupados por status
        Map<LeadStatus, Long> leadsPorStatus = obterLeadsPorStatus(tenantId, startDateTime, endDateTime);
        log.debug("Leads por status: {}", leadsPorStatus);

        // Construir o DTO com o builder
        return MetricsDTO.builder()
                .totalLeadsGeral(totalLeadsGeral)
                .totalLeadsPeriodo(totalLeadsPeriodo)
                .taxaConversao(taxaConversao)
                .receitaPotencial(receitaPotencial)
                .ganhoNoPeriodo(ganhoNoPeriodo)
                .leadsPorStatus(leadsPorStatus)
                .build();
    }

    /**
     * Calcula a taxa de conversão (leads ganhos / total de leads no período)
     * Evita divisão por zero retornando 0.0 se não houver leads no período
     *
     * @param leadsGanhos Quantidade de leads com status GANHO
     * @param totalLeads Total de leads no período
     * @return Percentual entre 0 e 100
     */
    private double calcularTaxaConversao(long leadsGanhos, long totalLeads) {
        if (totalLeads == 0) {
            log.warn("Sem leads no período - taxa de conversão será 0%");
            return 0.0;
        }

        double taxa = ((double) leadsGanhos / totalLeads) * 100;
        return Math.round(taxa * 100.0) / 100.0; // Arredondar para 2 casas decimais
    }

    /**
     * Recupera o agrupamento de leads por status dentro do período
     * Converte a lista de mapas retornada pelo repository em um Map<LeadStatus, Long>
     * Inclui tratamentos de segurança para null e conversão de tipos
     *
     * @param tenantId ID da empresa (tenant)
     * @param startDate Data de início
     * @param endDate Data de fim
     * @return Mapa com status -> contagem
     */
    private Map<LeadStatus, Long> obterLeadsPorStatus(UUID tenantId, LocalDateTime startDate, LocalDateTime endDate) {
        Map<LeadStatus, Long> resultado = new HashMap<>();

        // Inicializar com todos os status possíveis (padrão 0)
        for (LeadStatus status : LeadStatus.values()) {
            resultado.put(status, 0L);
        }

        try {
            // Buscar os dados agrupados do banco
            List<Map<String, Object>> leadsAgrupados = leadRepository.countLeadsByStatusInPeriod(tenantId, startDate, endDate);

            // Validação de segurança: verificar se o resultado não é nulo
            if (leadsAgrupados == null || leadsAgrupados.isEmpty()) {
                log.debug("Nenhum agrupamento de status encontrado para tenant: {}", tenantId);
                return resultado;
            }

            // Preencher com os valores reais
            for (Map<String, Object> item : leadsAgrupados) {
                // Validação de segurança: verificar se o item não é nulo
                if (item == null) {
                    log.warn("Item nulo encontrado no agrupamento de status");
                    continue;
                }

                // Extrair e validar o status
                Object statusObj = item.get("status");
                if (statusObj == null) {
                    log.warn("Campo 'status' é nulo no resultado da query");
                    continue;
                }

                // Extrair e validar a contagem
                Object countObj = item.get("count");
                if (countObj == null) {
                    log.warn("Campo 'count' é nulo no resultado da query");
                    continue;
                }

                try {
                    // Converter o status para String (pode ser String ou LeadStatus)
                    String statusStr = statusObj instanceof String
                        ? (String) statusObj
                        : statusObj.toString();

                    // Converter a contagem para Long
                    Long count = countObj instanceof Long
                        ? (Long) countObj
                        : ((Number) countObj).longValue();

                    // Tentar converter a String para LeadStatus
                    LeadStatus status = LeadStatus.valueOf(statusStr);
                    resultado.put(status, count);

                } catch (IllegalArgumentException e) {
                    log.warn("Status inválido encontrado no banco de dados: '{}'. Erro: {}", statusObj, e.getMessage());
                } catch (ClassCastException e) {
                    log.error("Erro ao converter tipos do resultado da query. Status: {}, Count: {}. Erro: {}",
                        statusObj, countObj, e.getMessage(), e);
                }
            }

        } catch (Exception e) {
            log.error("Erro ao obter agrupamento de leads por status para tenant: {}", tenantId, e);
        }

        return resultado;
    }
}
