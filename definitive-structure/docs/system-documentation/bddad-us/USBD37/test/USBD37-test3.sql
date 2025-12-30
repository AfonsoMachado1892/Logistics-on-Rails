SET SERVEROUTPUT ON;
DECLARE
    -- Nome existente no USBD32 (ID 1104) com formatação diferente
v_duplicate_name VARCHAR2(100) := '  regmms 32 94 356 3  ';
    v_new_id         NUMBER := 8888;
BEGIN
    DBMS_OUTPUT.PUT_LINE('--- USBD37: TESTE 3 (Falha - Nome Duplicado) ---');

    pr_register_wagon_model(
        p_model_id      => v_new_id,
        p_model_name    => v_duplicate_name,
        p_maker         => 'Fabrica X',
        p_num_bogies    => 2, p_bogies_type => 'X', p_length_mm => 10, p_width_mm => 10, p_height_mm => 10, p_weight_tons => 10, p_max_speed_kmh => 10, p_payload_tons => 10, p_volume_m3 => 10, p_type_desc => 'X',
        p_gauge_id      => 1668
    );

    DBMS_OUTPUT.PUT_LINE('RESULTADO: FALHA. O sistema permitiu nome duplicado.');
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE = -20033 THEN
            DBMS_OUTPUT.PUT_LINE('RESULTADO: SUCESSO. Erro de Nome duplicado detetado corretamente.');
ELSE
            DBMS_OUTPUT.PUT_LINE('RESULTADO: FALHA. Erro inesperado: ' || SQLERRM);
END IF;
END;
/