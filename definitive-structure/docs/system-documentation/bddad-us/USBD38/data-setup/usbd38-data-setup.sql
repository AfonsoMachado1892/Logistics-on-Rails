-- usbd38-data-setup.sql
-- -----------------------------------------------------------
-- USBD38: Data Setup - Preparação de dados para testes
-- IMPORTANTE: Este script NÃO insere dados, apenas prepara o ambiente
-- -----------------------------------------------------------

-- Limpar dados de teste anteriores (se existirem)
-- Estas bitolas serão usadas apenas para testes da função
DELETE FROM Railway_Gauge WHERE gauge_value IN (1000, 1520, 600, 1676);

-- Também limpar por nome (case-insensitive) para garantir ambiente limpo
DELETE FROM Railway_Gauge
WHERE UPPER(description) IN (
                             'BITOLA MÉTRICA',
                             'BITOLA RUSSA',
                             'BITOLA ESTREITA',
                             'OUTRA MÉTRICA',
                             'BITOLA INVÁLIDA',
                             'BITOLA ZERO'
    );

COMMIT;

-- Verificar bitolas existentes
PROMPT ========================================
PROMPT Bitolas existentes antes dos testes:
PROMPT ========================================
SELECT gauge_value AS "Largura (mm)",
       description AS "Descrição"
FROM Railway_Gauge
ORDER BY gauge_value;

PROMPT
PROMPT ========================================
PROMPT Ambiente preparado para testes
PROMPT Bitolas a testar: 1000mm, 1520mm, 600mm, 1676mm
PROMPT Bitolas existentes (não alterar): 1668mm, 1435mm
PROMPT ========================================
PROMPT