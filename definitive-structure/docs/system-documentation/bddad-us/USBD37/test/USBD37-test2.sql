BEGIN
    DBMS_OUTPUT.PUT_LINE('--- TESTE 2: Tentar registar um Modelo com ID já existente ---');

BEGIN
        -- Tenta registar o Modelo 1104 (que já existe nos dados iniciais do script usbd32.sql)
        pr_register_wagon_model(
            p_model_id => 1104,
            p_model_name => 'Duplicado',
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

        -- Se chegar aqui, falhou
        DBMS_OUTPUT.PUT_LINE('RESULTADO: FALHA. O sistema permitiu duplicar um ID de modelo.');

EXCEPTION
        WHEN OTHERS THEN
            -- Espera-se o erro -20031 (definido na implementação anterior)
            IF SQLCODE = -20031 THEN
                DBMS_OUTPUT.PUT_LINE('RESULTADO: SUCESSO. O sistema detetou o ID duplicado (Erro -20031).');
ELSE
                DBMS_OUTPUT.PUT_LINE('RESULTADO: FALHA. Erro incorreto: ' || SQLERRM);
END IF;
END;
END;
/