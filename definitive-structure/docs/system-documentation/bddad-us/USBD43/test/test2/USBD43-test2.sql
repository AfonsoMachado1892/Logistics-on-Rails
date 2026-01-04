SET SERVEROUTPUT ON;
DECLARE
v_id NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('--- TEST 2: Duplicate Name Detection ---');
    -- Limpeza
DELETE FROM Locomotive_Model WHERE id IN (200, 201) OR UPPER(model_name) = 'INDEPENDENT TEST MODEL';
COMMIT;

v_id := fn_create_electric_loco_model(200, 'Independent Test Model', 'Siemens', 6400, 87, 230, 300, '25kV', '50Hz', 'Bitola Ibérica', 19.2, 3.0, 4.4, 2, 'Bo-Bo');

BEGIN
        v_id := fn_create_electric_loco_model(201, '   INDEPENDENT    test    MODEL   ', 'Siemens', 6400, 87, 230, 300, '25kV', '50Hz', 'Bitola Ibérica', 19.2, 3.0, 4.4, 2, 'Bo-Bo');
        DBMS_OUTPUT.PUT_LINE('Test Failed: Duplicate allowed.');
EXCEPTION
        WHEN OTHERS THEN
            IF SQLCODE = -20001 THEN
                DBMS_OUTPUT.PUT_LINE('Success: Duplicate blocked correctly.');
ELSE RAISE; END IF;
END;
END;
/