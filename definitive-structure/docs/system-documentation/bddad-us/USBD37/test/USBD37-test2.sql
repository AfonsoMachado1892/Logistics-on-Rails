SET SERVEROUTPUT ON;
DECLARE
v_existing_id NUMBER := 1104; -- Já existe
BEGIN
    DBMS_OUTPUT.PUT_LINE('--- USBD37: TESTE 2 (Falha - ID Duplicado) ---');

    pr_register_wagon_model(
        p_model_id      => v_existing_id,
        p_model_name    => 'Nome Qualquer',
        p_maker         => 'Fabrica X',
        p_num_bogies    => 2, p_bogies_type => 'X', p_length_mm => 10, p_width_mm => 10, p_height_mm => 10, p_weight_tons => 10, p_max_speed_kmh => 10, p_payload_tons => 10, p_volume_m3 => 10, p_type_desc => 'X',
        p_gauge_id      => 1668
    );

    DBMS_OUTPUT.PUT_LINE('RESULTADO: FALHA. O sistema permitiu duplicar ID.');
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE = -20031 THEN
            DBMS_OUTPUT.PUT_LINE('RESULTADO: SUCESSO. Erro de ID duplicado detetado corretamente.');
ELSE
            DBMS_OUTPUT.PUT_LINE('RESULTADO: FALHA. Erro inesperado: ' || SQLERRM);
END IF;
END;
/