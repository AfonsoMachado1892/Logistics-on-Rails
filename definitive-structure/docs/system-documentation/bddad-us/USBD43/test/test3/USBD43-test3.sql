SET SERVEROUTPUT ON;
DECLARE
v_id NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('--- TEST 3: Non-Existent Gauge ---');
BEGIN
        v_id := fn_create_electric_loco_model(102, 'Train 3000 X', 'Alstom', 5000, 90, 120, 250, '25kV', '50Hz', 'Mars Gauge', 19.2, 3.0, 4.4, 2, 'Bo-Bo');
EXCEPTION
        WHEN OTHERS THEN
            IF SQLCODE = -20002 THEN
                DBMS_OUTPUT.PUT_LINE('Success: Invalid Gauge blocked.');
ELSE RAISE; END IF;
END;
END;
/