SET SERVEROUTPUT ON;
DECLARE
v_id NUMBER;
    v_check NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('--- TEST 4: Duplicate against pre-existing data ---');

SELECT COUNT(*) INTO v_check FROM Facility WHERE name = 'Valença';

IF v_check = 0 THEN
        DBMS_OUTPUT.PUT_LINE('WARNING: "Valença" not found in DB. Please run USBD32.sql first.');
        RETURN; -- Exit test if data is missing
END IF;

    -- Attempt to insert 'Valença' again using different casing
    -- Input: "  VALENÇA  " -> Should match "Valença"
BEGIN
        DBMS_OUTPUT.PUT_LINE('Attempting to insert duplicate "  VALENÇA  "...');
        v_id := fn_register_facility('  VALENÇA  ');

        DBMS_OUTPUT.PUT_LINE('RESULT: FAILURE. Duplicate "Valença" was accepted.');
EXCEPTION
        WHEN OTHERS THEN
            IF SQLCODE = -20002 THEN
                DBMS_OUTPUT.PUT_LINE('RESULT: SUCCESS. "Valença" was correctly identified as a duplicate.');
ELSE
                DBMS_OUTPUT.PUT_LINE('RESULT: FAILURE. Unexpected error: ' || SQLERRM);
END IF;
END;
END;
/