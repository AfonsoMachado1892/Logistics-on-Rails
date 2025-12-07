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
    v_id_gauge GAUGE.ID_GAUGE%TYPE;
    v_count    NUMBER;
BEGIN
    -- 1. Validação: Campos obrigatórios
    IF p_gauge_name IS NULL OR p_width_mm IS NULL THEN
        RETURN 0; -- Falha: Campos obrigatórios em falta
END IF;

    -- 2. Regra de Negócio: Verificar unicidade do nome (case-insensitive)
SELECT COUNT(*) INTO v_count
FROM GAUGE
WHERE UPPER(NAME) = UPPER(p_gauge_name);

IF v_count > 0 THEN
        RETURN -1; -- Falha: Nome da bitola já existe
END IF;

    -- 3. Regra de Negócio: Verificar se a largura é um número positivo
    IF p_width_mm <= 0 THEN
        RETURN -2; -- Falha: Largura inválida
END IF;

    -- 4. Obter o próximo ID da sequence
SELECT GAUGE_SEQ.NEXTVAL INTO v_id_gauge FROM DUAL;

-- 5. Inserir a nova bitola na tabela GAUGE
INSERT INTO GAUGE (ID_GAUGE, NAME, WIDTH_MM)
VALUES (v_id_gauge, p_gauge_name, p_width_mm);

-- 6. Commit da transação e retorno de sucesso
COMMIT;
RETURN 1; -- Sucesso
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
RETURN 0; -- Falha Geral
END insert_gauge;
/