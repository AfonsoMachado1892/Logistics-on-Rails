SET SERVEROUTPUT ON;
DECLARE
v_id NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('--- Test 2: Duplicate Detection (Independent) ---');

    -- STEP 1: Insert a "Base" Model to check against (ID 200)
    DBMS_OUTPUT.PUT_LINE('Step 1: Inserting Base Model (ID 200)...');
    v_id := fn_create_electric_loco_model(
        p_model_id      => 200,
        p_model_name    => 'Independent Test Model',
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

    -- STEP 2: Attempt to insert the same name with crazy spacing (ID 201)
    DBMS_OUTPUT.PUT_LINE('Step 2: Attempting to insert duplicate name (ID 201)...');

BEGIN
        v_id := fn_create_electric_loco_model(
            p_model_id      => 201,
            p_model_name    => '   INDEPENDENT    test    MODEL   ', -- Matches "Independent Test Model"
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

        -- If we reach here, the function FAILED to block the duplicate
        DBMS_OUTPUT.PUT_LINE('Test Failed: Duplicate Name was accepted.');

EXCEPTION
        WHEN OTHERS THEN
            -- We expect error -20001 (Duplicate Model)
            IF SQLCODE = -20001 THEN
                DBMS_OUTPUT.PUT_LINE('Success: Duplicate correctly detected.');
                DBMS_OUTPUT.PUT_LINE('Error Message Received: ' || SQLERRM);
ELSE
                DBMS_OUTPUT.PUT_LINE('Test Failed: Unexpected error: ' || SQLERRM);
END IF;
END;

    -- STEP 3: Cleanup (Rollback so we don't pollute the DB with ID 200)
ROLLBACK;
DBMS_OUTPUT.PUT_LINE('Cleanup: Transaction Rolled Back.');
END;
/