SET SERVEROUTPUT ON;
DECLARE
v_id NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('--- TEST 2: Duplicate Detection (Case & Spaces) ---');

    -- 1. Insert base record
    v_id := fn_register_facility('Aveiro Test Node');
    DBMS_OUTPUT.PUT_LINE('Step 1: Inserted "Aveiro Test Node"');

    -- 2. Attempt duplicate with messy input
    -- "  AVEIRO   TEST   NODE  " should be caught as a duplicate
BEGIN
        v_id := fn_register_facility('  AVEIRO   TEST   NODE  ');
        DBMS_OUTPUT.PUT_LINE('RESULT: FAILURE. System allowed duplicate.');
EXCEPTION
        WHEN OTHERS THEN
            IF SQLCODE = -20002 THEN
                DBMS_OUTPUT.PUT_LINE('RESULT: SUCCESS. Caught expected duplicate error.');
ELSE
                DBMS_OUTPUT.PUT_LINE('RESULT: FAILURE. Wrong error code: ' || SQLCODE || ' - ' || SQLERRM);
END IF;
END;
END;
/