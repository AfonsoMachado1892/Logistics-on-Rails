SET SERVEROUTPUT ON;
DECLARE
v_id NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('--- Test 6: Empty/Whitespace Model Name ---');

BEGIN
        v_id := fn_create_electric_loco_model(
            p_model_id      => 105,
            p_model_name    => '   ',  -- Invalid
            p_make          => 'Alstom',
            p_power_kw      => 5000,
            p_weight_tons   => 90,
            p_max_speed_kmh => 120,
            p_traction_kn   => 250,
            p_voltage       => '25kV',
            p_frequency     => '50Hz',
            p_gauge_desc    => 'Bitola Ibérica',
            p_length_m      => 19.2,
            p_width_m       => 3.0,
            p_height_m      => 4.4,
            p_num_bogies    => 2,
            p_bogies_type   => 'Bo-Bo'
        );
        DBMS_OUTPUT.PUT_LINE('Test 6 Failed: Accepted Empty/Whitespace Name.');
EXCEPTION
        WHEN OTHERS THEN
            IF SQLCODE = -20003 THEN
                DBMS_OUTPUT.PUT_LINE('Success: Empty string correctly blocked.');
ELSE
                DBMS_OUTPUT.PUT_LINE('Test 6 Failed with unexpected error: ' || SQLERRM);
END IF;
END;
END;
/