/* ==========================================================================
   USBD45 - Test Case 3: Foreign Key Validation (Non-existent References)
   ========================================================================== */

-- Enable output
SET SERVEROUTPUT ON;

-- Test 3.1: Try to add a line with non-existent owner (should fail)
DECLARE
v_result VARCHAR2(500);
BEGIN
    DBMS_OUTPUT.PUT_LINE('===== Test 3.1: Non-existent owner (should fail) =====');
    v_result := add_new_line(
        p_line_id => 300,
        p_line_name => 'Line with Invalid Owner',
        p_owner_vat => 'PT999999999', -- Does not exist
        p_start_facility_id => 5,
        p_end_facility_id => 8,
        p_gauge => 1668,
        p_segment_id => 3000,
        p_electrified => 'Yes',
        p_max_weight_kg_m => 8000,
        p_length_m => 10000,
        p_number_tracks => 2
    );
    DBMS_OUTPUT.PUT_LINE(v_result);
    DBMS_OUTPUT.PUT_LINE('');
END;
/

-- Test 3.2: Try to add a line with non-existent start facility (should fail)
DECLARE
v_result VARCHAR2(500);
BEGIN
    DBMS_OUTPUT.PUT_LINE('===== Test 3.2: Non-existent start facility (should fail) =====');
    v_result := add_new_line(
        p_line_id => 301,
        p_line_name => 'Line with Invalid Start Facility',
        p_owner_vat => 'PT503933813',
        p_start_facility_id => 9999, -- Does not exist
        p_end_facility_id => 8,
        p_gauge => 1668,
        p_segment_id => 3001,
        p_electrified => 'Yes',
        p_max_weight_kg_m => 8000,
        p_length_m => 10000,
        p_number_tracks => 2
    );
    DBMS_OUTPUT.PUT_LINE(v_result);
    DBMS_OUTPUT.PUT_LINE('');
END;
/

-- Test 3.3: Try to add a line with non-existent end facility (should fail)
DECLARE
v_result VARCHAR2(500);
BEGIN
    DBMS_OUTPUT.PUT_LINE('===== Test 3.3: Non-existent end facility (should fail) =====');
    v_result := add_new_line(
        p_line_id => 302,
        p_line_name => 'Line with Invalid End Facility',
        p_owner_vat => 'PT503933813',
        p_start_facility_id => 5,
        p_end_facility_id => 9999, -- Does not exist
        p_gauge => 1668,
        p_segment_id => 3002,
        p_electrified => 'Yes',
        p_max_weight_kg_m => 8000,
        p_length_m => 10000,
        p_number_tracks => 2
    );
    DBMS_OUTPUT.PUT_LINE(v_result);
    DBMS_OUTPUT.PUT_LINE('');
END;
/

-- Test 3.4: Try to add a line with non-existent gauge (should fail)
DECLARE
v_result VARCHAR2(500);
BEGIN
    DBMS_OUTPUT.PUT_LINE('===== Test 3.4: Non-existent gauge (should fail) =====');
    v_result := add_new_line(
        p_line_id => 303,
        p_line_name => 'Line with Invalid Gauge',
        p_owner_vat => 'PT503933813',
        p_start_facility_id => 5,
        p_end_facility_id => 8,
        p_gauge => 9999, -- Does not exist
        p_segment_id => 3003,
        p_electrified => 'Yes',
        p_max_weight_kg_m => 8000,
        p_length_m => 10000,
        p_number_tracks => 2
    );
    DBMS_OUTPUT.PUT_LINE(v_result);
    DBMS_OUTPUT.PUT_LINE('');
END;
/