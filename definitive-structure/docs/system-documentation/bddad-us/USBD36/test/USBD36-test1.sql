SET SERVEROUTPUT ON;
DECLARE
v_id NUMBER;
    v_name VARCHAR2(50) := 'Armazém Logístico Norte';
BEGIN
    DBMS_OUTPUT.PUT_LINE('--- TESTE 1: Inserir Armazém em Leixões (ID 50) ---');

    -- 1. Executar a ação (Manual ID 1001)
    pr_add_building_to_facility(
        p_building_id => 1001,
        p_facility_id => 50,
        p_name        => v_name,
        p_type        => 'Warehouse'
    );

    -- 2. Validar o resultado IMEDIATAMENTE
BEGIN
SELECT id INTO v_id
FROM Building
WHERE id = 1001;

DBMS_OUTPUT.PUT_LINE('RESULTADO: SUCESSO. Registo encontrado com ID: ' || v_id);
EXCEPTION
        WHEN NO_DATA_FOUND THEN
            DBMS_OUTPUT.PUT_LINE('RESULTADO: FALHA. O registo não foi encontrado na tabela.');
END;

ROLLBACK; -- Cleanup
END;
/