SET SERVEROUTPUT ON;
DECLARE
v_id NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('--- TEST 2: Normalization (Case & Spaces) ---');
    -- Limpeza de IDs de teste
DELETE FROM Facility WHERE id IN (902, 903) OR UPPER(name) LIKE '%PORTO TEST HUB%';
COMMIT;

v_id := fn_register_facility(902, 'Porto Test Hub');

BEGIN
        v_id := fn_register_facility(903, '   PORTO   test   HUB   ');
        DBMS_OUTPUT.PUT_LINE('RESULT: FAILURE. Allowed duplicate.');
EXCEPTION
        WHEN OTHERS THEN
            IF SQLCODE = -20004 THEN
                DBMS_OUTPUT.PUT_LINE('RESULT: SUCCESS. Caught duplicate.');
ELSE RAISE;
END IF;
END;
END;
/