SET SERVEROUTPUT ON;
DECLARE
v_id NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('--- TEST 5: Empty Spaces Validation ---');
BEGIN
        v_id := fn_register_facility(906, '      ');
        DBMS_OUTPUT.PUT_LINE('RESULT: FAILURE.');
EXCEPTION
        WHEN OTHERS THEN
            IF SQLCODE = -20002 THEN
                DBMS_OUTPUT.PUT_LINE('RESULT: SUCESSO (Espaços bloqueados).');
ELSE DBMS_OUTPUT.PUT_LINE('RESULT: FALHA: ' || SQLERRM);
END IF;
END;
END;
/