/* ==========================================================================
   USBD45 - Test Case 4: Business Logic Validation
   ========================================================================== */

-- Enable output
SET SERVEROUTPUT ON;

-- Test 4.1: Try to add a line with same start and end facility (should fail)
DECLARE
v_result VARCHAR2(500);
BEGIN
    DBMS_OUTPUT.PUT_LINE('===== Test 4.1: Same start/end facility (should fail) =====');
    v_result := add_new_line(
        p_line_id => 400,
        p_line_name => 'Line with Same Facilities',
        p_owner_vat => 'PT503933813',
        p_start_facility_id => 5,
        p_end_facility_id => 5, -- Same as start
        p_gauge => 1668,
        p_segment_id => 4000,
        p_electrified => 'Yes',
        p_max_weight_kg_m => 8000,
        p_length_m => 10000,
        p_number_tracks => 2
    );
    DBMS_OUTPUT.PUT_LINE(v_result);
    DBMS_OUTPUT.PUT_LINE('');
END;
/

-- Test 4.2: Try to add a line with invalid electrified value (should fail)
DECLARE
v_result VARCHAR2(500);
BEGIN
    DBMS_OUTPUT.PUT_LINE('===== Test 4.2: Invalid electrified value (should fail) =====');
    v_result := add_new_line(
        p_line_id => 401,
        p_line_name => 'Line with Invalid Electrified',
        p_owner_vat => 'PT503933813',
        p_start_facility_id => 5,
        p_end_facility_id => 8,
        p_gauge => 1668,
        p_segment_id => 4001,
        p_electrified => 'Maybe', -- Invalid
        p_max_weight_kg_m => 8000,
        p_length_m => 10000,
        p_number_tracks => 2
    );
    DBMS_OUTPUT.PUT_LINE(v_result);
    DBMS_OUTPUT.PUT_LINE('');
END;
/

-- Test 4.3: Try to add a line with invalid number of tracks (should fail)
DECLARE
v_result VARCHAR2(500);
BEGIN
    DBMS_OUTPUT.PUT_LINE('===== Test 4.3: Invalid number of tracks (should fail) =====');
    v_result := add_new_line(
        p_line_id => 402,
        p_line_name => 'Line with Invalid Tracks',
        p_owner_vat => 'PT503933813',
        p_start_facility_id => 5,
        p_end_facility_id => 8,
        p_gauge => 1668,
        p_segment_id => 4002,
        p_electrified => 'Yes',
        p_max_weight_kg_m => 8000,
        p_length_m => 10000,
        p_number_tracks => 3 -- Invalid, must be 1 or 2
    );
    DBMS_OUTPUT.PUT_LINE(v_result);
    DBMS_OUTPUT.PUT_LINE('');
END;
/

-- Test 4.4: Try to add a line with zero length (should fail)
DECLARE
v_result VARCHAR2(500);
BEGIN
    DBMS_OUTPUT.PUT_LINE('===== Test 4.4: Zero segment length (should fail) =====');
    v_result := add_new_line(
        p_line_id => 403,
        p_line_name => 'Line with Zero Length',
        p_owner_vat => 'PT503933813',
        p_start_facility_id => 5,
        p_end_facility_id => 8,
        p_gauge => 1668,
        p_segment_id => 4003,
        p_electrified => 'Yes',
        p_max_weight_kg_m => 8000,
        p_length_m => 0, -- Invalid
        p_number_tracks => 2
    );
    DBMS_OUTPUT.PUT_LINE(v_result);
    DBMS_OUTPUT.PUT_LINE('');
END;
/

-- Test 4.5: Try to add a line with negative length (should fail)
DECLARE
v_result VARCHAR2(500);
BEGIN
    DBMS_OUTPUT.PUT_LINE('===== Test 4.5: Negative segment length (should fail) =====');
    v_result := add_new_line(
        p_line_id => 404,
        p_line_name => 'Line with Negative Length',
        p_owner_vat => 'PT503933813',
        p_start_facility_id => 5,
        p_end_facility_id => 8,
        p_gauge => 1668,
        p_segment_id => 4004,
        p_electrified => 'Yes',
        p_max_weight_kg_m => 8000,
        p_length_m => -1000, -- Invalid
        p_number_tracks => 2
    );
    DBMS_OUTPUT.PUT_LINE(v_result);
    DBMS_OUTPUT.PUT_LINE('');
END;
/

-- Test 4.6: Try to add a line with negative max weight (should fail)
DECLARE
v_result VARCHAR2(500);
BEGIN
    DBMS_OUTPUT.PUT_LINE('===== Test 4.6: Negative max weight (should fail) =====');
    v_result := add_new_line(
        p_line_id => 405,
        p_line_name => 'Line with Negative Weight',
        p_owner_vat => 'PT503933813',
        p_start_facility_id => 5,
        p_end_facility_id => 8,
        p_gauge => 1668,
        p_segment_id => 4005,
        p_electrified => 'Yes',
        p_max_weight_kg_m => -8000, -- Invalid
        p_length_m => 10000,
        p_number_tracks => 2
    );
    DBMS_OUTPUT.PUT_LINE(v_result);
    DBMS_OUTPUT.PUT_LINE('');
END;
/