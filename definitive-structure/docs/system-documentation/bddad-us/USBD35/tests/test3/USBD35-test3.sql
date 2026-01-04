SET SERVEROUTPUT ON;
DECLARE
v_id NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('--- TEST 3: Null Input Validation ---');

BEGIN
        -- Attempt to register with Valid ID (904) but NULL Name
        v_id := fn_register_facility(904, NULL);

        DBMS_OUTPUT.PUT_LINE('RESULT: FAILURE. System allowed NULL name.');
EXCEPTION
        WHEN OTHERS THEN
            -- New Error Code for Null Name is -20002
            IF SQLCODE = -20002 THEN
                DBMS_OUTPUT.PUT_LINE('RESULT: SUCCESS (NULL input blocked correctly).');
                DBMS_OUTPUT.PUT_LINE('Message: ' || SQLERRM);
ELSE
                DBMS_OUTPUT.PUT_LINE('RESULT: FAILURE (Unexpected error code): ' || SQLERRM);
END IF;
END;
END;
/