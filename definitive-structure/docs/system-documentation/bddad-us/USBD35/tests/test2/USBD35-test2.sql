SET SERVEROUTPUT ON;
DECLARE
v_id NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('--- TEST 2: Normalization (Case & Spaces) ---');

    -- 1. Insert base record
    v_id := fn_register_facility('Porto Test Hub');
    DBMS_OUTPUT.PUT_LINE('Step 1: Inserted "Porto Test Hub"');

    -- 2. Attempt duplicate with messy casing and spaces
    BEGIN
        v_id := fn_register_facility('   PORTO   test   HUB   ');
        DBMS_OUTPUT.PUT_LINE('RESULT: FAILURE. System allowed duplicate with different casing/spaces.');
    EXCEPTION
        WHEN OTHERS THEN
            IF SQLCODE = -20002 THEN
                DBMS_OUTPUT.PUT_LINE('RESULT: SUCCESS. Correctly blocked duplicate via normalization.');
            ELSE
                DBMS_OUTPUT.PUT_LINE('RESULT: FAILURE. Wrong error code: ' || SQLERRM);
            END IF;
    END;
END;
/