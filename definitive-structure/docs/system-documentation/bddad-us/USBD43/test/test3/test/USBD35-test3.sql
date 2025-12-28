SET SERVEROUTPUT ON;
DECLARE
v_id NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('--- TEST 3: Null/Empty Input Validation ---');

    -- Test 3a: NULL Input
BEGIN
        DBMS_OUTPUT.PUT_LINE('Testing NULL input...');
        v_id := fn_register_facility(NULL);
        DBMS_OUTPUT.PUT_LINE('RESULT: FAILURE. System accepted NULL.');
EXCEPTION
        WHEN OTHERS THEN
            IF SQLCODE = -20001 THEN
                DBMS_OUTPUT.PUT_LINE('RESULT: SUCCESS (NULL blocked).');
ELSE
                DBMS_OUTPUT.PUT_LINE('RESULT: FAILURE (Wrong Error): ' || SQLERRM);
END IF;
END;

    -- Test 3b: Empty Spaces
BEGIN
        DBMS_OUTPUT.PUT_LINE('Testing Empty String (spaces)...');
        v_id := fn_register_facility('   ');
        DBMS_OUTPUT.PUT_LINE('RESULT: FAILURE. System accepted empty spaces.');
EXCEPTION
        WHEN OTHERS THEN
            IF SQLCODE = -20001 THEN
                DBMS_OUTPUT.PUT_LINE('RESULT: SUCCESS (Empty spaces blocked).');
ELSE
                DBMS_OUTPUT.PUT_LINE('RESULT: FAILURE (Wrong Error): ' || SQLERRM);
END IF;
END;
END;
/