CREATE OR REPLACE FUNCTION fn_create_electric_loco_model(
    p_model_id      IN NUMBER,
    p_model_name    IN VARCHAR2,
    p_make          IN VARCHAR2,
    p_power_kw      IN NUMBER,
    p_weight_tons   IN NUMBER,
    p_max_speed_kmh IN NUMBER,
    p_traction_kn   IN NUMBER,
    p_voltage       IN VARCHAR2,
    p_frequency     IN VARCHAR2,
    p_gauge_desc    IN VARCHAR2,
    p_length_m      IN NUMBER,
    p_width_m       IN NUMBER,
    p_height_m      IN NUMBER,
    p_num_bogies    IN NUMBER,
    p_bogies_type   IN VARCHAR2
) RETURN NUMBER IS
    v_count         NUMBER;
    v_gauge_val     NUMBER;
    v_clean_name    VARCHAR2(100);
    v_norm_name     VARCHAR2(100);

BEGIN
    -- 1. Validação de Inputs Obrigatórios
    IF p_model_id IS NULL OR TRIM(p_model_name) IS NULL OR TRIM(p_make) IS NULL OR TRIM(p_gauge_desc) IS NULL THEN
        RAISE_APPLICATION_ERROR(-20003, 'Mandatory fields cannot be null or empty.');
END IF;

    -- 2. Validar valores numéricos (Devem ser positivos)
    IF p_power_kw <= 0 OR p_weight_tons <= 0 OR p_max_speed_kmh <= 0 OR p_traction_kn <= 0 THEN
        RAISE_APPLICATION_ERROR(-20003, 'Numeric values must be positive.');
END IF;

    -- 3. Verificar se o ID já existe
SELECT COUNT(*) INTO v_count FROM Locomotive_Model WHERE id = p_model_id;
IF v_count > 0 THEN
        RAISE_APPLICATION_ERROR(-20004, 'Model ID already exists.');
END IF;

    -- 4. Normalização e Verificação de Nome Duplicado (Case/Space Insensitive)
    v_clean_name := REGEXP_REPLACE(TRIM(p_model_name), '\s+', ' ');
    v_norm_name  := UPPER(v_clean_name);

SELECT COUNT(*) INTO v_count FROM Locomotive_Model
WHERE UPPER(REGEXP_REPLACE(TRIM(model_name), '\s+', ' ')) = v_norm_name;

IF v_count > 0 THEN
        RAISE_APPLICATION_ERROR(-20001, 'Locomotive model name already exists.');
END IF;

    -- 5. Validar Bitola (Gauge)
BEGIN
SELECT gauge_value INTO v_gauge_val FROM Railway_Gauge WHERE description = TRIM(p_gauge_desc);
EXCEPTION
        WHEN NO_DATA_FOUND THEN
            RAISE_APPLICATION_ERROR(-20002, 'Gauge description not found.');
END;

    -- 6. Inserção
INSERT INTO Locomotive_Model (
    id, model_name, make, power_kw, weight_tons, max_speed_kmh, traction_kn,
    engine_type, voltage, frequency, gauge, length_m, width_m, height_m,
    num_bogies, bogies_type
) VALUES (
             p_model_id, v_clean_name, TRIM(p_make), p_power_kw, p_weight_tons,
             p_max_speed_kmh, p_traction_kn, 'Electric', TRIM(p_voltage), TRIM(p_frequency),
             v_gauge_val, p_length_m, p_width_m, p_height_m, p_num_bogies, TRIM(p_bogies_type)
         );

-- 7. Gestão de Transação: Sucesso
COMMIT;
RETURN p_model_id;

EXCEPTION
    WHEN OTHERS THEN
        -- 8. Gestão de Transação: Falha
        ROLLBACK;
        RAISE;
END;
/