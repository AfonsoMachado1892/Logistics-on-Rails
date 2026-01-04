SET SERVEROUTPUT ON;
DECLARE
v_id NUMBER;
    v_test_id NUMBER := 901;
    v_name VARCHAR2(100) := 'Coimbra Logistics Hub';
BEGIN
    DBMS_OUTPUT.PUT_LINE('--- TEST 1: Register Valid Facility ---');
    -- Limpeza preventiva
DELETE FROM Facility WHERE id = v_test_id OR UPPER(name) = UPPER(v_name);
COMMIT;

v_id := fn_register_facility(v_test_id, v_name);
    DBMS_OUTPUT.PUT_LINE('RESULT: SUCCESS. Returned ID: ' || v_id);
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('RESULT: FAILURE. Error: ' || SQLERRM);
END;
/