-- usbd38-data-setup.sql
-- -----------------------------------------------------------
-- USBD38: Data Setup - Preparação de dados para testes
-- -----------------------------------------------------------

-- Limpar dados de teste anteriores (se existirem)
DELETE FROM Railway_Gauge WHERE gauge_value IN (1000, 1520, 600, 1676);
COMMIT;

-- Verificar bitolas existentes
PROMPT ========================================
PROMPT Bitolas existentes antes dos testes:
PROMPT ========================================
SELECT gauge_value AS "Largura (mm)",
       description AS "Descrição"
FROM Railway_Gauge
ORDER BY gauge_value;