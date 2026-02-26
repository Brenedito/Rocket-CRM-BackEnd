package com.rocket.crm.controllers;

import com.rocket.crm.config.security.UserContext;
import com.rocket.crm.dtos.MetricsDTO;
import com.rocket.crm.services.MetricsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/metrics")
@RequiredArgsConstructor
@Slf4j
public class MetricsController {

    private final MetricsService metricsService;
    private final UserContext userContext;

    /**
     * Retorna as métricas de leads para um período específico
     *
     * @param startDate Data de início do período (formato: yyyy-MM-dd)
     * @param endDate Data de fim do período (formato: yyyy-MM-dd)
     * @return MetricsDTO com todos os dados calculados
     */
    @GetMapping
    public ResponseEntity<MetricsDTO> getMetrics(
            @RequestParam("startDate")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,

            @RequestParam("endDate")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate) {

        log.info("Requisição de métricas recebida: startDate={}, endDate={}", startDate, endDate);

        // Obtém o tenant_id do contexto do usuário autenticado
        UUID tenantId = userContext.getTenantId();

        if (tenantId == null) {
            log.error("Tenant ID não encontrado no contexto do usuário");
            return ResponseEntity.badRequest().build();
        }

        // Validar que startDate é anterior a endDate
        if (startDate.isAfter(endDate)) {
            log.warn("Data de início ({}) é posterior à data de fim ({})", startDate, endDate);
            return ResponseEntity.badRequest().build();
        }

        try {
            MetricsDTO metrics = metricsService.getMetricsByPeriod(tenantId, startDate, endDate);
            log.info("Métricas calculadas com sucesso para tenant: {}", tenantId);
            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            log.error("Erro ao calcular métricas para tenant: {}", tenantId, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
