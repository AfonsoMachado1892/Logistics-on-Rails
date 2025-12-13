-- usbd38-script.sql
-- -----------------------------------------------------------
-- USBD38: Function to add a new gauge (Bitola)
-- -----------------------------------------------------------

CREATE OR REPLACE FUNCTION insert_gauge (
    p_gauge_name IN VARCHAR2,
    p_width_mm   IN NUMBER
)
RETURN NUMBER
IS
    v_count NUMBER;
BEGIN
    -- 1. Validação: Campos obrigatórios
    IF p_gauge_name IS NULL OR p_width_mm IS NULL THEN
        RETURN 0; -- Falha: Campos obrigatórios em falta
END IF;

    -- 2. Regra de Negócio: Verificar unicidade da largura (PK)
SELECT COUNT(*) INTO v_count
FROM Railway_Gauge
WHERE gauge_value = p_width_mm;

IF v_count > 0 THEN
        RETURN -1; -- Falha: Bitola com esta largura já existe
END IF;

    -- 3. Regra de Negócio: Verificar se a largura é um número positivo
    IF p_width_mm <= 0 THEN
        RETURN -2; -- Falha: Largura inválida
END IF;

    -- 4. Verificar unicidade do nome (case-insensitive)
SELECT COUNT(*) INTO v_count
FROM Railway_Gauge
WHERE UPPER(description) = UPPER(p_gauge_name);

IF v_count > 0 THEN
        RETURN -3; -- Falha: Nome da bitola já existe
END IF;

    -- 5. Inserir a nova bitola na tabela Railway_Gauge
INSERT INTO Railway_Gauge (gauge_value, description)
VALUES (p_width_mm, p_gauge_name);

-- 6. Commit da transação e retorno de sucesso
COMMIT;
RETURN 1; -- Sucesso

EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
RETURN 0; -- Falha Geral
END insert_gauge;
/