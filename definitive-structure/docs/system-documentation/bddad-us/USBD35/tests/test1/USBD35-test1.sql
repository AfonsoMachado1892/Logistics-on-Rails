SET SERVEROUTPUT ON;
DECLARE
v_id NUMBER;
    v_name VARCHAR2(100) := 'Coimbra Logistics Hub';
    v_verify_name VARCHAR2(100);
BEGIN
    DBMS_OUTPUT.PUT_LINE('--- TEST 1: Register Valid Facility ---');

    -- 1. Execute Function
    v_id := fn_register_facility(v_name);
    DBMS_OUTPUT.PUT_LINE('Returned ID: ' || v_id);

    -- 2. Verify existence in DB
SELECT name INTO v_verify_name FROM Facility WHERE id = v_id;

IF v_verify_name = v_name THEN
        DBMS_OUTPUT.PUT_LINE('RESULT: SUCCESS. Name matches input exactly.');
ELSE
        DBMS_OUTPUT.PUT_LINE('RESULT: FAILURE. Name mismatch.');
END IF;

EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('RESULT: FAILURE. Unexpected Error: ' || SQLERRM);
END;
/