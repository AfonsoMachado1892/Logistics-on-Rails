SET SERVEROUTPUT ON;
DECLARE
v_count NUMBER;
    v_name VARCHAR2(50) := 'Silo de Cereais A';
BEGIN
    DBMS_OUTPUT.PUT_LINE('--- TESTE 2: Inserir Silo em Valença (ID 11) ---');

    -- Manual ID 1002
    pr_add_building_to_facility(1002, 11, v_name, 'Silo');

    -- Validação por contagem
SELECT COUNT(*) INTO v_count
FROM Building
WHERE id = 1002 AND facility_id = 11 AND name = v_name AND type = 'Silo';

IF v_count = 1 THEN
        DBMS_OUTPUT.PUT_LINE('RESULTADO: SUCESSO. O Silo foi inserido corretamente.');
ELSE
        DBMS_OUTPUT.PUT_LINE('RESULTADO: FALHA. O Silo não aparece na base de dados.');
END IF;

ROLLBACK; -- Cleanup
END;
/