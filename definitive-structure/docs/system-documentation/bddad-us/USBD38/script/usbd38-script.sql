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
        RETURN 0;
END IF;

    -- 2. Regra de Negócio: Verificar se a largura é um número positivo
    IF p_width_mm <= 0 THEN
        RETURN -2;
END IF;

    -- 3. Regra de Negócio: Verificar se o nome da bitola já existe (case-insensitive)
SELECT COUNT(*) INTO v_count
FROM Railway_Gauge
WHERE UPPER(description) = UPPER(p_gauge_name);

IF v_count > 0 THEN
        -- Retorna -3 para nome duplicado (conforme esperado no Teste 3b)
        RETURN -3;
END IF;

    -- 4. Inserir a nova bitola
INSERT INTO Railway_Gauge (gauge_value, description)
VALUES (p_width_mm, p_gauge_name);

COMMIT;
RETURN 1;

EXCEPTION
    WHEN DUP_VAL_ON_INDEX THEN
        ROLLBACK;
        -- Retorna -1 para valor de bitola (gauge_value/PK) duplicado
RETURN -1;
WHEN OTHERS THEN
        ROLLBACK;
RETURN 0;
END insert_gauge;
/