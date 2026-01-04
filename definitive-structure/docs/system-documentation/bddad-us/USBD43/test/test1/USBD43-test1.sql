SET SERVEROUTPUT ON;
DECLARE
v_id NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('--- Test 1: Happy Path ---');
    v_id := fn_create_electric_loco_model(
        p_model_id      => 100,
        p_model_name    => 'EuroSprinter ES64',
        p_make          => 'Siemens',
        p_power_kw      => 6400,
        p_weight_tons   => 87,
        p_max_speed_kmh => 230,
        p_traction_kn   => 300,
        p_voltage       => '25kV',
        p_frequency     => '50Hz',
        p_gauge_desc    => 'Bitola Ibérica',
        p_length_m      => 19.2,
        p_width_m       => 3.0,
        p_height_m      => 4.4,
        p_num_bogies    => 2,
        p_bogies_type   => 'Bo-Bo'
    );
    DBMS_OUTPUT.PUT_LINE('Success! Created Electric Locomotive Model with ID: ' || v_id);
COMMIT;
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('Test Failed: ' || SQLERRM);
ROLLBACK;
END;
/