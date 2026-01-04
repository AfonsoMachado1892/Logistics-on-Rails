SET SERVEROUTPUT ON;
DECLARE
v_id NUMBER;
    v_test_id NUMBER := 100;
BEGIN
    DBMS_OUTPUT.PUT_LINE('--- TEST 1: Happy Path ---');
    -- Limpeza preventiva
DELETE FROM Locomotive_Model WHERE id = v_test_id OR UPPER(model_name) = 'EUROSPRINTER ES64';
COMMIT;

v_id := fn_create_electric_loco_model(v_test_id, 'EuroSprinter ES64', 'Siemens', 6400, 87, 230, 300, '25kV', '50Hz', 'Bitola Ibérica', 19.2, 3.0, 4.4, 2, 'Bo-Bo');
    DBMS_OUTPUT.PUT_LINE('Success! Created ID: ' || v_id);
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('Test Failed: ' || SQLERRM);
END;
/