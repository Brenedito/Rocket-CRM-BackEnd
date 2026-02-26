package com.rocket.crm.services;

import com.rocket.crm.dtos.MetricsDTO;
import com.rocket.crm.enums.LeadStatus;
import com.rocket.crm.repositories.LeadRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("Testes do MetricsService")
class MetricsServiceTest {

    @Mock
    private LeadRepository leadRepository;

    @InjectMocks
    private MetricsService metricsService;

    private UUID tenantId;
    private LocalDate startDate;
    private LocalDate endDate;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        tenantId = UUID.randomUUID();
        startDate = LocalDate.of(2026, 2, 1);
        endDate = LocalDate.of(2026, 2, 28);
    }

    @Test
    @DisplayName("Deve calcular métricas corretamente quando há dados no período")
    void testGetMetricsByPeriodComDados() {
        // Arrange
        when(leadRepository.countTotalLeads(tenantId)).thenReturn(100L);
        when(leadRepository.countLeadsByPeriod(eq(tenantId), any(), any())).thenReturn(50L);
        when(leadRepository.sumLeadValueByPeriod(eq(tenantId), any(), any()))
                .thenReturn(new BigDecimal("100000.00"));
        when(leadRepository.countWonLeadsByPeriod(eq(tenantId), any(), any())).thenReturn(15L);
        when(leadRepository.sumWonLeadValueByPeriod(eq(tenantId), any(), any()))
                .thenReturn(new BigDecimal("45000.00"));

        List<Map<String, Object>> statusGrouping = createStatusGrouping();
        when(leadRepository.countLeadsByStatusInPeriod(eq(tenantId), any(), any()))
                .thenReturn(statusGrouping);

        // Act
        MetricsDTO result = metricsService.getMetricsByPeriod(tenantId, startDate, endDate);

        // Assert
        assertNotNull(result);
        assertEquals(100L, result.getTotalLeadsGeral());
        assertEquals(50L, result.getTotalLeadsPeriodo());
        assertEquals(new BigDecimal("100000.00"), result.getReceitaPotencial());
        assertEquals(new BigDecimal("45000.00"), result.getGanhoNoPeriodo());
        assertEquals(30.0, result.getTaxaConversao()); // 15/50 * 100
        assertNotNull(result.getLeadsPorStatus());

        // Verify all repository methods were called
        verify(leadRepository, times(1)).countTotalLeads(tenantId);
        verify(leadRepository, times(1)).countLeadsByPeriod(eq(tenantId), any(), any());
        verify(leadRepository, times(1)).sumLeadValueByPeriod(eq(tenantId), any(), any());
        verify(leadRepository, times(1)).countWonLeadsByPeriod(eq(tenantId), any(), any());
        verify(leadRepository, times(1)).sumWonLeadValueByPeriod(eq(tenantId), any(), any());
        verify(leadRepository, times(1)).countLeadsByStatusInPeriod(eq(tenantId), any(), any());
    }

    @Test
    @DisplayName("Deve retornar taxa de conversão 0% quando não há leads no período")
    void testTaxaConversaoZeroQuandoSemLeads() {
        // Arrange
        when(leadRepository.countTotalLeads(tenantId)).thenReturn(100L);
        when(leadRepository.countLeadsByPeriod(eq(tenantId), any(), any())).thenReturn(0L);
        when(leadRepository.sumLeadValueByPeriod(eq(tenantId), any(), any()))
                .thenReturn(BigDecimal.ZERO);
        when(leadRepository.countWonLeadsByPeriod(eq(tenantId), any(), any())).thenReturn(0L);
        when(leadRepository.sumWonLeadValueByPeriod(eq(tenantId), any(), any()))
                .thenReturn(BigDecimal.ZERO);
        when(leadRepository.countLeadsByStatusInPeriod(eq(tenantId), any(), any()))
                .thenReturn(new ArrayList<>());

        // Act
        MetricsDTO result = metricsService.getMetricsByPeriod(tenantId, startDate, endDate);

        // Assert
        assertEquals(0.0, result.getTaxaConversao());
        assertEquals(0L, result.getTotalLeadsPeriodo());
    }

    @Test
    @DisplayName("Deve evitar divisão por zero para taxa de conversão")
    void testEvitaDivisaoPorZero() {
        // Arrange
        when(leadRepository.countTotalLeads(tenantId)).thenReturn(0L);
        when(leadRepository.countLeadsByPeriod(eq(tenantId), any(), any())).thenReturn(0L);
        when(leadRepository.sumLeadValueByPeriod(eq(tenantId), any(), any()))
                .thenReturn(BigDecimal.ZERO);
        when(leadRepository.countWonLeadsByPeriod(eq(tenantId), any(), any())).thenReturn(0L);
        when(leadRepository.sumWonLeadValueByPeriod(eq(tenantId), any(), any()))
                .thenReturn(BigDecimal.ZERO);
        when(leadRepository.countLeadsByStatusInPeriod(eq(tenantId), any(), any()))
                .thenReturn(new ArrayList<>());

        // Act & Assert - não deve lançar ArithmeticException
        assertDoesNotThrow(() ->
            metricsService.getMetricsByPeriod(tenantId, startDate, endDate)
        );
    }

    @Test
    @DisplayName("Deve retornar todos os status com 0 quando não há dados")
    void testRetornaStatusComZero() {
        // Arrange
        when(leadRepository.countTotalLeads(tenantId)).thenReturn(50L);
        when(leadRepository.countLeadsByPeriod(eq(tenantId), any(), any())).thenReturn(10L);
        when(leadRepository.sumLeadValueByPeriod(eq(tenantId), any(), any()))
                .thenReturn(new BigDecimal("50000.00"));
        when(leadRepository.countWonLeadsByPeriod(eq(tenantId), any(), any())).thenReturn(2L);
        when(leadRepository.sumWonLeadValueByPeriod(eq(tenantId), any(), any()))
                .thenReturn(new BigDecimal("10000.00"));
        when(leadRepository.countLeadsByStatusInPeriod(eq(tenantId), any(), any()))
                .thenReturn(new ArrayList<>()); // Sem dados

        // Act
        MetricsDTO result = metricsService.getMetricsByPeriod(tenantId, startDate, endDate);

        // Assert
        assertNotNull(result.getLeadsPorStatus());
        assertEquals(7, result.getLeadsPorStatus().size()); // Todos os 7 status

        // Todos devem iniciar com 0
        for (LeadStatus status : LeadStatus.values()) {
            assertEquals(0L, result.getLeadsPorStatus().get(status));
        }
    }

    @Test
    @DisplayName("Deve mapear corretamente os status de leads")
    void testMapeiaStatusCorretamente() {
        // Arrange
        when(leadRepository.countTotalLeads(tenantId)).thenReturn(100L);
        when(leadRepository.countLeadsByPeriod(eq(tenantId), any(), any())).thenReturn(35L);
        when(leadRepository.sumLeadValueByPeriod(eq(tenantId), any(), any()))
                .thenReturn(new BigDecimal("175000.00"));
        when(leadRepository.countWonLeadsByPeriod(eq(tenantId), any(), any())).thenReturn(10L);
        when(leadRepository.sumWonLeadValueByPeriod(eq(tenantId), any(), any()))
                .thenReturn(new BigDecimal("50000.00"));

        List<Map<String, Object>> statusGrouping = Arrays.asList(
                createStatusMap("NOVO", 5),
                createStatusMap("CONTATO", 8),
                createStatusMap("QUALIFICADO", 7),
                createStatusMap("GANHO", 10),
                createStatusMap("PERDIDO", 5)
        );
        when(leadRepository.countLeadsByStatusInPeriod(eq(tenantId), any(), any()))
                .thenReturn(statusGrouping);

        // Act
        MetricsDTO result = metricsService.getMetricsByPeriod(tenantId, startDate, endDate);

        // Assert
        Map<LeadStatus, Long> statusMap = result.getLeadsPorStatus();
        assertEquals(5L, statusMap.get(LeadStatus.NOVO));
        assertEquals(8L, statusMap.get(LeadStatus.CONTATO));
        assertEquals(7L, statusMap.get(LeadStatus.QUALIFICADO));
        assertEquals(10L, statusMap.get(LeadStatus.GANHO));
        assertEquals(5L, statusMap.get(LeadStatus.PERDIDO));
        assertEquals(0L, statusMap.get(LeadStatus.PROPOSTA));
        assertEquals(0L, statusMap.get(LeadStatus.NEGOCIACAO));
    }

    @Test
    @DisplayName("Deve arredondar taxa de conversão para 2 casas decimais")
    void testArredondaTaxaConversao() {
        // Arrange
        when(leadRepository.countTotalLeads(tenantId)).thenReturn(100L);
        when(leadRepository.countLeadsByPeriod(eq(tenantId), any(), any())).thenReturn(3L);
        when(leadRepository.sumLeadValueByPeriod(eq(tenantId), any(), any()))
                .thenReturn(new BigDecimal("15000.00"));
        when(leadRepository.countWonLeadsByPeriod(eq(tenantId), any(), any())).thenReturn(1L); // 1/3 = 33.333...%
        when(leadRepository.sumWonLeadValueByPeriod(eq(tenantId), any(), any()))
                .thenReturn(new BigDecimal("5000.00"));
        when(leadRepository.countLeadsByStatusInPeriod(eq(tenantId), any(), any()))
                .thenReturn(new ArrayList<>());

        // Act
        MetricsDTO result = metricsService.getMetricsByPeriod(tenantId, startDate, endDate);

        // Assert - deve arredondar para 33.33%
        assertEquals(33.33, result.getTaxaConversao());
    }

    @Test
    @DisplayName("Deve tratar BigDecimal null corretamente")
    void testTrataNullBigDecimal() {
        // Arrange
        when(leadRepository.countTotalLeads(tenantId)).thenReturn(50L);
        when(leadRepository.countLeadsByPeriod(eq(tenantId), any(), any())).thenReturn(20L);
        when(leadRepository.sumLeadValueByPeriod(eq(tenantId), any(), any())).thenReturn(null);
        when(leadRepository.countWonLeadsByPeriod(eq(tenantId), any(), any())).thenReturn(5L);
        when(leadRepository.sumWonLeadValueByPeriod(eq(tenantId), any(), any())).thenReturn(null);
        when(leadRepository.countLeadsByStatusInPeriod(eq(tenantId), any(), any()))
                .thenReturn(new ArrayList<>());

        // Act
        MetricsDTO result = metricsService.getMetricsByPeriod(tenantId, startDate, endDate);

        // Assert
        assertEquals(BigDecimal.ZERO, result.getReceitaPotencial());
        assertEquals(BigDecimal.ZERO, result.getGanhoNoPeriodo());
    }

    @Test
    @DisplayName("Deve calcular taxa de conversão 100% quando todos os leads são ganhos")
    void testTaxaConversao100Porcento() {
        // Arrange
        when(leadRepository.countTotalLeads(tenantId)).thenReturn(100L);
        when(leadRepository.countLeadsByPeriod(eq(tenantId), any(), any())).thenReturn(50L);
        when(leadRepository.sumLeadValueByPeriod(eq(tenantId), any(), any()))
                .thenReturn(new BigDecimal("100000.00"));
        when(leadRepository.countWonLeadsByPeriod(eq(tenantId), any(), any())).thenReturn(50L); // Todos ganhos
        when(leadRepository.sumWonLeadValueByPeriod(eq(tenantId), any(), any()))
                .thenReturn(new BigDecimal("100000.00"));
        when(leadRepository.countLeadsByStatusInPeriod(eq(tenantId), any(), any()))
                .thenReturn(Arrays.asList(createStatusMap("GANHO", 50)));

        // Act
        MetricsDTO result = metricsService.getMetricsByPeriod(tenantId, startDate, endDate);

        // Assert
        assertEquals(100.0, result.getTaxaConversao());
    }

    // ===== Métodos Auxiliares =====

    private List<Map<String, Object>> createStatusGrouping() {
        return Arrays.asList(
                createStatusMap("NOVO", 10),
                createStatusMap("CONTATO", 12),
                createStatusMap("QUALIFICADO", 8),
                createStatusMap("PROPOSTA", 5),
                createStatusMap("GANHO", 15)
        );
    }

    private Map<String, Object> createStatusMap(String status, long count) {
        Map<String, Object> map = new HashMap<>();
        map.put("status", status);
        map.put("count", count);
        return map;
    }
}
