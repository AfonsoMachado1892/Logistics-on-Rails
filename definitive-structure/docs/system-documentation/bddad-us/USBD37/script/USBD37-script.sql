CREATE OR REPLACE PROCEDURE pr_register_wagon_model(
    p_model_id      IN NUMBER,
    p_model_name    IN VARCHAR2,
    p_maker         IN VARCHAR2,
    p_num_bogies    IN NUMBER,
    p_bogies_type   IN VARCHAR2,
    p_length_mm     IN NUMBER,
    p_width_mm      IN NUMBER,
    p_height_mm     IN NUMBER,
    p_weight_tons   IN NUMBER,
    p_max_speed_kmh IN NUMBER,
    p_payload_tons  IN NUMBER,
    p_volume_m3     IN NUMBER,
    p_type_desc     IN VARCHAR2,
    p_gauge_id      IN NUMBER      -- CORREÇÃO: Apenas UM parâmetro para bitola
) IS
    v_count NUMBER;
BEGIN
    -- 1. Validação de parâmetros obrigatórios básicos
    IF p_model_id IS NULL OR p_model_name IS NULL OR p_gauge_id IS NULL THEN
        RAISE_APPLICATION_ERROR(-20030, 'USBD37: ID, Nome do Modelo e Bitola são obrigatórios.');
END IF;

    -- 2. Verificar se o ID do modelo já existe
SELECT COUNT(*) INTO v_count FROM Wagon_Model WHERE id = p_model_id;
IF v_count > 0 THEN
        RAISE_APPLICATION_ERROR(-20031, 'USBD37: O Modelo de Vagão ID ' || p_model_id || ' já existe.');
END IF;

    -- 3. Verificar se o Nome do modelo já existe (Case Insensitive)
    -- Feedback: "Comparar apenas os nomes diretamente não vale nada."
SELECT COUNT(*) INTO v_count
FROM Wagon_Model
WHERE UPPER(TRIM(model_name)) = UPPER(TRIM(p_model_name));

IF v_count > 0 THEN
        RAISE_APPLICATION_ERROR(-20033, 'USBD37: Já existe um modelo com o nome "' || p_model_name || '".');
END IF;

    -- 4. Inserir na tabela principal Wagon_Model
INSERT INTO Wagon_Model (
    id, model_name, maker, num_bogies, bogies_type, length_mm, width_mm,
    height_mm, weight_tons, max_speed_kmh, payload_tons, volume_m3, type_desc, gauge
) VALUES (
             p_model_id, TRIM(p_model_name), TRIM(p_maker), p_num_bogies, TRIM(p_bogies_type), p_length_mm, p_width_mm,
             p_height_mm, p_weight_tons, p_max_speed_kmh, p_payload_tons, p_volume_m3, TRIM(p_type_desc), p_gauge_id
         );

-- 5. Inserir na tabela de associação (Wagon_Model_Gauge)
-- Garante que a bitola é registada como suportada.
INSERT INTO Wagon_Model_Gauge (wagon_model_id, gauge_value)
VALUES (p_model_id, p_gauge_id);

DBMS_OUTPUT.PUT_LINE('Sucesso: Modelo ' || p_model_id || ' registado com a bitola ' || p_gauge_id);
COMMIT;

EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE;
END;
/