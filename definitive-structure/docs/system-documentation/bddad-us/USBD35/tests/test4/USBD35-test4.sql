SET SERVEROUTPUT ON;
DECLARE
    v_id NUMBER;
    v_exists NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('--- TEST 4: Duplicate against USBD32 Seed Data ---');

    -- Check if 'Valença' exists from USBD32 script
    SELECT COUNT(*) INTO v_exists FROM Facility WHERE UPPER(name) = 'VALENÇA';

    IF v_exists = 0 THEN
        DBMS_OUTPUT.PUT_LINE('SKIPPING TEST: "Valença" not found. Run USBD32.sql first.');
    ELSE
        BEGIN
            DBMS_OUTPUT.PUT_LINE('Attempting to insert "  valença  " (duplicate of seed data)...');
            v_id := fn_register_facility('  valença  ');
            DBMS_OUTPUT.PUT_LINE('RESULT: FAILURE. System allowed duplicate of seed data.');
        EXCEPTION
            WHEN OTHERS THEN
                IF SQLCODE = -20002 THEN
                    DBMS_OUTPUT.PUT_LINE('RESULT: SUCCESS. Identified "Valença" as a duplicate.');
                ELSE
                    DBMS_OUTPUT.PUT_LINE('RESULT: FAILURE. Unexpected error: ' || SQLERRM);
                END IF;
        END;
    END IF;
END;
/