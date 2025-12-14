/* ==========================================================================
   USBD45 - Test Case 1: Success Cases
   ========================================================================== */

-- Enable output
SET SERVEROUTPUT ON;

-- Clean up test data before running tests
DELETE FROM Segment WHERE id >= 1000;
DELETE FROM Line WHERE id >= 100;
COMMIT;

-- Test 1.1: Successfully add a new line with double track, no siding
DECLARE
v_result VARCHAR2(500);
BEGIN
    DBMS_OUTPUT.PUT_LINE('===== Test 1.1: Add line with double track (no siding) =====');
    v_result := add_new_line(
        p_line_id => 100,
        p_line_name => 'Ramal Porto-Barcelos',
        p_owner_vat => 'PT503933813',
        p_start_facility_id => 5,
        p_end_facility_id => 8,
        p_gauge => 1668,
        p_segment_id => 1000,
        p_electrified => 'Yes',
        p_max_weight_kg_m => 8000,
        p_length_m => 15000,
        p_number_tracks => 2,
        p_siding_position => NULL,
        p_siding_length => NULL
    );
    DBMS_OUTPUT.PUT_LINE(v_result);
    DBMS_OUTPUT.PUT_LINE('');
END;
/

-- Test 1.2: Successfully add a line with single track and siding
DECLARE
v_result VARCHAR2(500);
BEGIN
    DBMS_OUTPUT.PUT_LINE('===== Test 1.2: Add line with single track and siding =====');
    v_result := add_new_line(
        p_line_id => 101,
        p_line_name => 'Ramal Midões-São Frutuoso',
        p_owner_vat => 'PT503933813',
        p_start_facility_id => 10,
        p_end_facility_id => 15,
        p_gauge => 1668,
        p_segment_id => 1001,
        p_electrified => 'Yes',
        p_max_weight_kg_m => 6400,
        p_length_m => 8000,
        p_number_tracks => 1,
        p_siding_position => 4000,
        p_siding_length => 500
    );
    DBMS_OUTPUT.PUT_LINE(v_result);
    DBMS_OUTPUT.PUT_LINE('');
END;
/

-- Test 1.3: Successfully add a non-electrified line
DECLARE
v_result VARCHAR2(500);
BEGIN
    DBMS_OUTPUT.PUT_LINE('===== Test 1.3: Add non-electrified line =====');
    v_result := add_new_line(
        p_line_id => 102,
        p_line_name => 'Ramal Nine-Braga',
        p_owner_vat => 'PT503933813',
        p_start_facility_id => 20,
        p_end_facility_id => 30,
        p_gauge => 1668,
        p_segment_id => 1002,
        p_electrified => 'No',
        p_max_weight_kg_m => 7000,
        p_length_m => 12000,
        p_number_tracks => 1
    );
    DBMS_OUTPUT.PUT_LINE(v_result);
    DBMS_OUTPUT.PUT_LINE('');
END;
/

-- Verify the results
DBMS_OUTPUT.PUT_LINE('');
DBMS_OUTPUT.PUT_LINE('===== VERIFICATION: Successfully created lines =====');
SELECT id, name, owner_vat, start_facility_id, end_facility_id, gauge
FROM Line WHERE id >= 100 ORDER BY id;

DBMS_OUTPUT.PUT_LINE('');
DBMS_OUTPUT.PUT_LINE('===== VERIFICATION: Successfully created segments =====');
SELECT id, line_id, sequence_order, electrified, length_m, number_tracks, siding_position, siding_length
FROM Segment WHERE id >= 1000 ORDER BY id;