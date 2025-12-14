DECLARE
v_id NUMBER;
    v_name VARCHAR2(50) := 'Armazém Logístico Norte';
BEGIN
    DBMS_OUTPUT.PUT_LINE('--- TESTE 1: Inserir Armazém em Leixões (ID 50) ---');

    -- 1. Executar a ação
    pr_add_building_to_facility(50, v_name, 'Warehouse');

    -- 2. Validar o resultado IMEDIATAMENTE
BEGIN
SELECT id INTO v_id
FROM Building
WHERE facility_id = 50 AND name = v_name;

DBMS_OUTPUT.PUT_LINE('RESULTADO: SUCESSO. Registo encontrado com ID: ' || v_id);
EXCEPTION
        WHEN NO_DATA_FOUND THEN
            DBMS_OUTPUT.PUT_LINE('RESULTADO: FALHA. O registo não foi encontrado na tabela.');
END;
END;
/