/* ==========================================================================
   IMPLEMENTAÇÃO USBD37: Registar um novo Modelo de Vagão e Bitolas Suportadas
   ========================================================================== */
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
    p_default_gauge IN NUMBER,     -- Bitola padrão (para a tabela Wagon_Model)
    p_gauge_list    IN VARCHAR2    -- Lista de outras bitolas separadas por vírgula (ex: '1435,1668')
) IS
    v_exists NUMBER;
    v_gauge_val NUMBER;
    v_str VARCHAR2(4000) := p_gauge_list;
    v_comma_pos NUMBER;
    v_current_gauge VARCHAR2(50);
BEGIN
    -- 1. Validação de parâmetros obrigatórios
    IF p_model_id IS NULL OR p_model_name IS NULL OR p_default_gauge IS NULL THEN
        RAISE_APPLICATION_ERROR(-20030, 'USBD37: ID, Nome do Modelo e Bitola Padrão são obrigatórios.');
END IF;

    -- 2. Verificar se o ID do modelo já existe
SELECT COUNT(*) INTO v_exists FROM Wagon_Model WHERE id = p_model_id;
IF v_exists > 0 THEN
        RAISE_APPLICATION_ERROR(-20031, 'USBD37: O Modelo de Vagão ID ' || p_model_id || ' já existe.');
END IF;

    -- 3. Inserir na tabela principal Wagon_Model
INSERT INTO Wagon_Model (
    id, model_name, maker, num_bogies, bogies_type, length_mm, width_mm,
    height_mm, weight_tons, max_speed_kmh, payload_tons, volume_m3, type_desc, gauge
) VALUES (
             p_model_id, p_model_name, p_maker, p_num_bogies, p_bogies_type, p_length_mm, p_width_mm,
             p_height_mm, p_weight_tons, p_max_speed_kmh, p_payload_tons, p_volume_m3, p_type_desc, p_default_gauge
         );

-- 4. Inserir a bitola padrão na tabela de associação (normalização)
-- Verifica se já existe (caso o trigger ou lógica anterior não trate)
INSERT INTO Wagon_Model_Gauge (wagon_model_id, gauge_value) VALUES (p_model_id, p_default_gauge);

-- 5. Processar e inserir bitolas adicionais da lista CSV
IF v_str IS NOT NULL THEN
        LOOP
            v_comma_pos := INSTR(v_str, ',');

            IF v_comma_pos > 0 THEN
                v_current_gauge := TRIM(SUBSTR(v_str, 1, v_comma_pos - 1));
                v_str := SUBSTR(v_str, v_comma_pos + 1);
ELSE
                v_current_gauge := TRIM(v_str);
                v_str := NULL;
END IF;

            -- Converte para numero e insere se não for igual à default (para evitar duplicados)
BEGIN
                v_gauge_val := TO_NUMBER(v_current_gauge);
                IF v_gauge_val != p_default_gauge THEN
                    INSERT INTO Wagon_Model_Gauge (wagon_model_id, gauge_value)
                    VALUES (p_model_id, v_gauge_val);
END IF;
EXCEPTION
                WHEN DUP_VAL_ON_INDEX THEN NULL; -- Ignora duplicados silenciosamente
WHEN OTHERS THEN
                    RAISE_APPLICATION_ERROR(-20032, 'USBD37: Erro ao processar bitola: ' || v_current_gauge);
END;

            EXIT WHEN v_str IS NULL;
END LOOP;
END IF;

    -- 6. Feedback e Commit
    DBMS_OUTPUT.PUT_LINE('Ação Realizada: Modelo de Vagão ' || p_model_name || ' registado com sucesso.');
COMMIT;

EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE;
END;
/