SET SERVEROUTPUT ON;
DECLARE
v_id NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('--- TEST 5: Empty String / Spaces Validation ---');

BEGIN
        -- Attempt to register with Valid ID (906) but Empty Spaces Name
        v_id := fn_register_facility(906, '      ');

        DBMS_OUTPUT.PUT_LINE('RESULT: FAILURE. System allowed empty spaces as name.');
EXCEPTION
        WHEN OTHERS THEN
            -- New Error Code for Empty Name is -20002
            IF SQLCODE = -20002 THEN
                DBMS_OUTPUT.PUT_LINE('RESULT: SUCCESS (Empty spaces blocked correctly).');
                DBMS_OUTPUT.PUT_LINE('Message: ' || SQLERRM);
ELSE
                DBMS_OUTPUT.PUT_LINE('RESULT: FAILURE (Unexpected error code): ' || SQLERRM);
END IF;
END;
END;
/