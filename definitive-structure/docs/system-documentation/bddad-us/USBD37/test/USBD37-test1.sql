DECLARE
v_count_model NUMBER;
    v_count_gauges NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('--- TESTE 1: Registar Novo Modelo de Vagão com Múltiplas Bitolas (Cenário de Sucesso) ---');

    -- 1. Executar a ação (Modelo 9001, Bitola base 1668, extra 1435)
    pr_register_wagon_model(
        p_model_id => 9001,
        p_model_name => 'Vagão Teste X',
        p_maker => 'Fabrica Teste',
        p_num_bogies => 4,
        p_bogies_type => 'Y25',
        p_length_mm => 15000,
        p_width_mm => 3000,
        p_height_mm => 4000,
        p_weight_tons => 25,
        p_max_speed_kmh => 120,
        p_payload_tons => 70,
        p_volume_m3 => 90,
        p_type_desc => 'Plataforma',
        p_default_gauge => 1668,
        p_gauge_list => '1435'
    );

    -- 2. Validar o resultado
    -- Deve existir 1 registo na tabela de modelos
SELECT COUNT(*) INTO v_count_model FROM Wagon_Model WHERE id = 9001;

-- Devem existir 2 registos na tabela de associação de bitolas (1668 e 1435)
SELECT COUNT(*) INTO v_count_gauges FROM Wagon_Model_Gauge WHERE wagon_model_id = 9001;

IF v_count_model = 1 AND v_count_gauges = 2 THEN
        DBMS_OUTPUT.PUT_LINE('RESULTADO: SUCESSO. O modelo e as suas 2 bitolas foram registados.');
ELSE
        DBMS_OUTPUT.PUT_LINE('RESULTADO: FALHA. Dados não encontrados corretamente (Modelo: ' || v_count_model || ', Bitolas: ' || v_count_gauges || ').');
END IF;

EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('RESULTADO: FALHA. Ocorreu um erro inesperado: ' || SQLERRM);
END;
/