SET SERVEROUTPUT ON;
DECLARE
v_id NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('--- TEST 2: Normalization (Case & Spaces) ---');

    -- STEP 0: PRE-TEST CLEANUP
    -- We delete IDs 902 and 903 first to ensure the test can run repeatedly without "ID exists" errors.
DELETE FROM Facility WHERE id IN (902, 903);

-- 1. Insert base record (ID 902)
v_id := fn_register_facility(902, 'Porto Test Hub');
    DBMS_OUTPUT.PUT_LINE('Step 1: Inserted "Porto Test Hub" with ID 902');

    -- 2. Attempt duplicate name with different casing/spaces (Using new ID 903)
BEGIN
        v_id := fn_register_facility(903, '   PORTO   test   HUB   ');
        DBMS_OUTPUT.PUT_LINE('RESULT: FAILURE. System allowed duplicate name.');
EXCEPTION
        WHEN OTHERS THEN
            -- We expect error -20004 for Duplicate Name
            IF SQLCODE = -20004 THEN
                DBMS_OUTPUT.PUT_LINE('RESULT: SUCCESS. Correctly blocked duplicate via normalization.');
ELSE
                DBMS_OUTPUT.PUT_LINE('RESULT: FAILURE. Wrong error code: ' || SQLERRM);
END IF;
END;

    -- STEP 3: POST-TEST CLEANUP
    -- Rollback ensures we don't leave junk data in the database after the test finishes.
ROLLBACK;
DBMS_OUTPUT.PUT_LINE('Cleanup: Transaction Rolled Back.');
END;
/