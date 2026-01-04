SET SERVEROUTPUT ON;
DECLARE
v_id NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('--- TEST 4: Seed Data Duplicate ---');
    -- Testa contra 'Valença' (carregada na USBD32)
BEGIN
        v_id := fn_register_facility(905, '  valença  ');
        DBMS_OUTPUT.PUT_LINE('RESULT: FAILURE.');
EXCEPTION
        WHEN OTHERS THEN
            IF SQLCODE = -20004 THEN
                DBMS_OUTPUT.PUT_LINE('RESULT: SUCESSO (Duplicado de Valença detetado).');
ELSE DBMS_OUTPUT.PUT_LINE('RESULT: FALHA: ' || SQLERRM);
END IF;
END;
END;
/