SET SERVEROUTPUT ON;
DECLARE
v_id NUMBER;
    v_name VARCHAR2(50) := 'Viana Test Station'; -- Safe name
    v_stored_name VARCHAR2(100);
BEGIN
    DBMS_OUTPUT.PUT_LINE('--- TEST 1: Insert Valid Facility ---');

    -- 1. Execute Function
    v_id := fn_register_facility(v_name);

    DBMS_OUTPUT.PUT_LINE('Returned ID: ' || v_id);

    -- 2. Verify Data
SELECT name INTO v_stored_name FROM Facility WHERE id = v_id;

IF v_stored_name = v_name THEN
        DBMS_OUTPUT.PUT_LINE('RESULT: SUCCESS. Name matches input exactly.');
ELSE
        DBMS_OUTPUT.PUT_LINE('RESULT: FAILURE. Name mismatch.');
END IF;

EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('RESULT: FAILURE. Error: ' || SQLERRM);
END;
/