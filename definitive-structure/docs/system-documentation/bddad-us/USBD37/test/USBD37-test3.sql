BEGIN
    DBMS_OUTPUT.PUT_LINE('--- TESTE 3: Tentar registar com parâmetros obrigatórios a NULL ---');

BEGIN
        -- Tenta registar sem ID e sem Nome
        pr_register_wagon_model(
            p_model_id => NULL,
            p_model_name => NULL,
            p_maker => 'Teste',
            p_num_bogies => 2,
            p_bogies_type => 'Y25',
            p_length_mm => 1000,
            p_width_mm => 1000,
            p_height_mm => 1000,
            p_weight_tons => 10,
            p_max_speed_kmh => 100,
            p_payload_tons => 10,
            p_volume_m3 => 10,
            p_type_desc => 'Erro',
            p_default_gauge => 1668,
            p_gauge_list => NULL
        );

        DBMS_OUTPUT.PUT_LINE('RESULTADO: FALHA. O sistema aceitou parâmetros NULL obrigatórios.');

EXCEPTION
        WHEN OTHERS THEN
            -- Espera-se o erro -20030
            IF SQLCODE = -20030 THEN
                DBMS_OUTPUT.PUT_LINE('RESULTADO: SUCESSO. O sistema validou os parâmetros obrigatórios (Erro -20030).');
ELSE
                DBMS_OUTPUT.PUT_LINE('RESULTADO: FALHA. Erro incorreto: ' || SQLERRM);
END IF;
END;
END;
/