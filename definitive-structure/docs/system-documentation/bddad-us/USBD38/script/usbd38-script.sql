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
    v_normalized_input VARCHAR2(200);
BEGIN
    -- 1. Validação: Campos obrigatórios
    IF p_gauge_name IS NULL OR p_width_mm IS NULL THEN
        RETURN 0;
END IF;

    -- 2. Regra de Negócio: Verificar se a largura é um número positivo
    IF p_width_mm <= 0 THEN
        RETURN -2;
END IF;

    -- 3. NORMALIZAÇÃO (Evitar a rejeição")
    -- Remove espaços no início/fim e transforma múltiplos espaços internos num só
    v_normalized_input := UPPER(TRIM(REGEXP_REPLACE(p_gauge_name, ' +', ' ')));

    -- 4. Regra de Negócio: Verificar se o nome já existe (com normalização rigorosa)
SELECT COUNT(*) INTO v_count
FROM Railway_Gauge
WHERE UPPER(TRIM(REGEXP_REPLACE(description, ' +', ' '))) = v_normalized_input;

IF v_count > 0 THEN
        RETURN -3; -- Nome duplicado
END IF;

    -- 5. Inserir a nova bitola
INSERT INTO Railway_Gauge (gauge_value, description)
VALUES (p_width_mm, TRIM(p_gauge_name));

COMMIT;
RETURN 1;

EXCEPTION
    WHEN DUP_VAL_ON_INDEX THEN
        ROLLBACK;
RETURN -1; -- Valor de bitola (PK) duplicado
WHEN OTHERS THEN
        ROLLBACK;
RETURN 0;
END insert_gauge;
/