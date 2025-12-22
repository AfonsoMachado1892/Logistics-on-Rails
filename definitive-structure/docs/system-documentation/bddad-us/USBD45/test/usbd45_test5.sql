/* ==========================================================================
   USBD45 - Test Case 5: Siding Validation
   ========================================================================== */

-- Enable output
SET SERVEROUTPUT ON;

-- Test 5.1: Try to add a line with siding position but no length (should fail)
DECLARE
v_result VARCHAR2(500);
BEGIN
    DBMS_OUTPUT.PUT_LINE('===== Test 5.1: Siding position without length (should fail) =====');
    v_result := add_new_line(
        p_line_id => 500,
        p_line_name => 'Line with Incomplete Siding 1',
        p_owner_vat => 'PT503933813',
        p_start_facility_id => 10,
        p_end_facility_id => 15,
        p_gauge => 1668,
        p_segment_id => 5000,
        p_electrified => 'Yes',
        p_max_weight_kg_m => 6400,
        p_length_m => 8000,
        p_number_tracks => 1,
        p_siding_position => 4000,
        p_siding_length => NULL -- Missing
    );
    DBMS_OUTPUT.PUT_LINE(v_result);
    DBMS_OUTPUT.PUT_LINE('');
END;
/

-- Test 5.2: Try to add a line with siding length but no position (should fail)
DECLARE
v_result VARCHAR2(500);
BEGIN
    DBMS_OUTPUT.PUT_LINE('===== Test 5.2: Siding length without position (should fail) =====');
    v_result := add_new_line(
        p_line_id => 501,
        p_line_name => 'Line with Incomplete Siding 2',
        p_owner_vat => 'PT503933813',
        p_start_facility_id => 10,
        p_end_facility_id => 15,
        p_gauge => 1668,
        p_segment_id => 5001,
        p_electrified => 'Yes',
        p_max_weight_kg_m => 6400,
        p_length_m => 8000,
        p_number_tracks => 1,
        p_siding_position => NULL, -- Missing
        p_siding_length => 500
    );
    DBMS_OUTPUT.PUT_LINE(v_result);
    DBMS_OUTPUT.PUT_LINE('');
END;
/

-- Test 5.3: Try to add a line with siding position greater than segment length (should fail)
DECLARE
v_result VARCHAR2(500);
BEGIN
    DBMS_OUTPUT.PUT_LINE('===== Test 5.3: Siding position > segment length (should fail) =====');
    v_result := add_new_line(
        p_line_id => 502,
        p_line_name => 'Line with Invalid Siding Position',
        p_owner_vat => 'PT503933813',
        p_start_facility_id => 10,
        p_end_facility_id => 15,
        p_gauge => 1668,
        p_segment_id => 5002,
        p_electrified => 'Yes',
        p_max_weight_kg_m => 6400,
        p_length_m => 8000,
        p_number_tracks => 1,
        p_siding_position => 9000, -- Greater than length
        p_siding_length => 500
    );
    DBMS_OUTPUT.PUT_LINE(v_result);
    DBMS_OUTPUT.PUT_LINE('');
END;
/

-- Test 5.4: Try to add a line with negative siding position (should fail)
DECLARE
v_result VARCHAR2(500);
BEGIN
    DBMS_OUTPUT.PUT_LINE('===== Test 5.4: Negative siding position (should fail) =====');
    v_result := add_new_line(
        p_line_id => 503,
        p_line_name => 'Line with Negative Siding Position',
        p_owner_vat => 'PT503933813',
        p_start_facility_id => 10,
        p_end_facility_id => 15,
        p_gauge => 1668,
        p_segment_id => 5003,
        p_electrified => 'Yes',
        p_max_weight_kg_m => 6400,
        p_length_m => 8000,
        p_number_tracks => 1,
        p_siding_position => -1000, -- Negative
        p_siding_length => 500
    );
    DBMS_OUTPUT.PUT_LINE(v_result);
    DBMS_OUTPUT.PUT_LINE('');
END;
/

-- Test 5.5: Try to add a line with zero siding length (should fail)
DECLARE
v_result VARCHAR2(500);
BEGIN
    DBMS_OUTPUT.PUT_LINE('===== Test 5.5: Zero siding length (should fail) =====');
    v_result := add_new_line(
        p_line_id => 504,
        p_line_name => 'Line with Zero Siding Length',
        p_owner_vat => 'PT503933813',
        p_start_facility_id => 10,
        p_end_facility_id => 15,
        p_gauge => 1668,
        p_segment_id => 5004,
        p_electrified => 'Yes',
        p_max_weight_kg_m => 6400,
        p_length_m => 8000,
        p_number_tracks => 1,
        p_siding_position => 4000,
        p_siding_length => 0 -- Invalid
    );
    DBMS_OUTPUT.PUT_LINE(v_result);
    DBMS_OUTPUT.PUT_LINE('');
END;
/

-- Test 5.6: Try to add a line with negative siding length (should fail)
DECLARE
v_result VARCHAR2(500);
BEGIN
    DBMS_OUTPUT.PUT_LINE('===== Test 5.6: Negative siding length (should fail) =====');
    v_result := add_new_line(
        p_line_id => 505,
        p_line_name => 'Line with Negative Siding Length',
        p_owner_vat => 'PT503933813',
        p_start_facility_id => 10,
        p_end_facility_id => 15,
        p_gauge => 1668,
        p_segment_id => 5005,
        p_electrified => 'Yes',
        p_max_weight_kg_m => 6400,
        p_length_m => 8000,
        p_number_tracks => 1,
        p_siding_position => 4000,
        p_siding_length => -500 -- Invalid
    );
    DBMS_OUTPUT.PUT_LINE(v_result);
    DBMS_OUTPUT.PUT_LINE('');
END;
/