SET SERVEROUTPUT ON;
DECLARE
v_new_model_id NUMBER := 8001; -- ID Novo
    v_gauge_id     NUMBER := 1668; -- Bitola Ibérica (Existe no USBD32)
    v_count_m      NUMBER;
    v_count_g      NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('--- USBD37: TESTE 1 (Sucesso) ---');

    -- Limpeza preventiva
DELETE FROM Wagon_Model_Gauge WHERE wagon_model_id = v_new_model_id;
DELETE FROM Wagon_Model WHERE id = v_new_model_id;

-- Executar Procedure
pr_register_wagon_model(
        p_model_id      => v_new_model_id,
        p_model_name    => 'Modelo Teste 8001',
        p_maker         => 'Fabrica Teste',
        p_num_bogies    => 4,
        p_bogies_type   => 'Y25',
        p_length_mm     => 14000,
        p_width_mm      => 3000,
        p_height_mm     => 4000,
        p_weight_tons   => 20,
        p_max_speed_kmh => 120,
        p_payload_tons  => 60,
        p_volume_m3     => 80,
        p_type_desc     => 'Contentor',
        p_gauge_id      => v_gauge_id
    );

    -- Validação
SELECT COUNT(*) INTO v_count_m FROM Wagon_Model WHERE id = v_new_model_id;
SELECT COUNT(*) INTO v_count_g FROM Wagon_Model_Gauge WHERE wagon_model_id = v_new_model_id AND gauge_value = v_gauge_id;

IF v_count_m = 1 AND v_count_g = 1 THEN
        DBMS_OUTPUT.PUT_LINE('RESULTADO: SUCESSO. Modelo criado e bitola associada.');
ELSE
        DBMS_OUTPUT.PUT_LINE('RESULTADO: FALHA. Dados não encontrados.');
END IF;

ROLLBACK; -- Deixa a BD limpa
END;
/