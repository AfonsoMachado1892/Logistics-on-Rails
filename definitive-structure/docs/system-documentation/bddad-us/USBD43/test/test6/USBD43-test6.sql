SET SERVEROUTPUT ON;
DECLARE
v_id NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('--- TEST 6: Empty/Whitespace Name ---');
BEGIN
        v_id := fn_create_electric_loco_model(105, '   ', 'Alstom', 5000, 90, 120, 250, '25kV', '50Hz', 'Bitola Ibérica', 19.2, 3.0, 4.4, 2, 'Bo-Bo');
EXCEPTION
        WHEN OTHERS THEN
            IF SQLCODE = -20003 THEN
                DBMS_OUTPUT.PUT_LINE('Success: Empty string blocked.');
ELSE RAISE; END IF;
END;
END;
/