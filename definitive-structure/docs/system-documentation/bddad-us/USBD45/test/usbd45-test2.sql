/* ==========================================================================
   USBD45 - Test Case 2: Duplicate ID Validation
   ========================================================================== */

-- Enable output
SET SERVEROUTPUT ON;

-- Setup: Create a line that will be used to test duplicates
DECLARE
v_result VARCHAR2(500);
BEGIN
    DBMS_OUTPUT.PUT_LINE('===== SETUP: Creating initial line for duplicate tests =====');
    v_result := add_new_line(
        p_line_id => 200,
        p_line_name => 'Original Line',
        p_owner_vat => 'PT503933813',
        p_start_facility_id => 5,
        p_end_facility_id => 8,
        p_gauge => 1668,
        p_segment_id => 2000,
        p_electrified => 'Yes',
        p_max_weight_kg_m => 8000,
        p_length_m => 10000,
        p_number_tracks => 2
    );
    DBMS_OUTPUT.PUT_LINE(v_result);
    DBMS_OUTPUT.PUT_LINE('');
END;
/

-- Test 2.1: Try to add a line with duplicate Line ID (should fail)
DECLARE
v_result VARCHAR2(500);
BEGIN
    DBMS_OUTPUT.PUT_LINE('===== Test 2.1: Duplicate Line ID (should fail) =====');
    v_result := add_new_line(
        p_line_id => 200, -- Already exists
        p_line_name => 'Duplicate Line',
        p_owner_vat => 'PT503933813',
        p_start_facility_id => 10,
        p_end_facility_id => 15,
        p_gauge => 1668,
        p_segment_id => 2001,
        p_electrified => 'Yes',
        p_max_weight_kg_m => 8000,
        p_length_m => 10000,
        p_number_tracks => 2
    );
    DBMS_OUTPUT.PUT_LINE(v_result);
    DBMS_OUTPUT.PUT_LINE('');
END;
/

-- Test 2.2: Try to add a line with duplicate Segment ID (should fail)
DECLARE
v_result VARCHAR2(500);
BEGIN
    DBMS_OUTPUT.PUT_LINE('===== Test 2.2: Duplicate Segment ID (should fail) =====');
    v_result := add_new_line(
        p_line_id => 201,
        p_line_name => 'New Line with Duplicate Segment',
        p_owner_vat => 'PT503933813',
        p_start_facility_id => 10,
        p_end_facility_id => 15,
        p_gauge => 1668,
        p_segment_id => 2000, -- Already exists
        p_electrified => 'Yes',
        p_max_weight_kg_m => 8000,
        p_length_m => 10000,
        p_number_tracks => 2
    );
    DBMS_OUTPUT.PUT_LINE(v_result);
    DBMS_OUTPUT.PUT_LINE('');
END;
/

-- Cleanup
DELETE FROM Segment WHERE id >= 2000;
DELETE FROM Line WHERE id >= 200;
COMMIT;