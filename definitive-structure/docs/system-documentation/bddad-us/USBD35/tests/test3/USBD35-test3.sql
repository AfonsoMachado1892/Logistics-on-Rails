SET SERVEROUTPUT ON;
DECLARE
v_id NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('--- TEST 3: Null Name Validation ---');
BEGIN
        v_id := fn_register_facility(904, NULL);
        DBMS_OUTPUT.PUT_LINE('RESULT: FAILURE.');
EXCEPTION
        WHEN OTHERS THEN
            IF SQLCODE = -20002 THEN
                DBMS_OUTPUT.PUT_LINE('RESULT: SUCESSO (NULL bloqueado).');
ELSE DBMS_OUTPUT.PUT_LINE('RESULT: FALHA (Erro inesperado): ' || SQLERRM);
END IF;
END;
END;
/