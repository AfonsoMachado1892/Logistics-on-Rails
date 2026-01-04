SET SERVEROUTPUT ON;
DECLARE
v_id NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('--- TEST 4: Negative Numeric Values ---');
BEGIN
        v_id := fn_create_electric_loco_model(103, 'Weak Train', 'Alstom', -100, 90, 120, 250, '25kV', '50Hz', 'Bitola Ibérica', 19.2, 3.0, 4.4, 2, 'Bo-Bo');
EXCEPTION
        WHEN OTHERS THEN
            IF SQLCODE = -20003 THEN
                DBMS_OUTPUT.PUT_LINE('Success: Negative value blocked.');
ELSE RAISE; END IF;
END;
END;
/