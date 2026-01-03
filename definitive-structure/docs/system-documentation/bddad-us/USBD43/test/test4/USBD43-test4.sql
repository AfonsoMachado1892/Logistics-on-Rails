SET SERVEROUTPUT ON;
DECLARE
v_id NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('--- Test 4: Negative Numeric Values ---');

BEGIN
        v_id := fn_create_electric_loco_model(
            p_model_id      => 103,
            p_model_name    => 'Weak Train',
            p_make          => 'Alstom',
            p_power_kw      => -100, -- Invalid
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
        DBMS_OUTPUT.PUT_LINE('Test 4 Failed: Accepted negative power.');
EXCEPTION
        WHEN OTHERS THEN
            IF SQLCODE = -20003 THEN
                DBMS_OUTPUT.PUT_LINE('Success: Negative value correctly blocked.');
ELSE
                DBMS_OUTPUT.PUT_LINE('Test 4 Failed with unexpected error: ' || SQLERRM);
END IF;
END;
END;
/