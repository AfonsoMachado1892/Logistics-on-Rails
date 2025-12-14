/* ==========================================================================
   USBD45 - Add New Line Function
   As a Planner, I want to add a new line. A line must have at least one segment.
   ========================================================================== */

CREATE OR REPLACE FUNCTION add_new_line(
    p_line_id IN NUMBER,
    p_line_name IN VARCHAR2,
    p_owner_vat IN VARCHAR2,
    p_start_facility_id IN NUMBER,
    p_end_facility_id IN NUMBER,
    p_gauge IN NUMBER,
    -- Segment parameters (at least one segment must be provided)
    p_segment_id IN NUMBER,
    p_electrified IN CHAR,
    p_max_weight_kg_m IN NUMBER,
    p_length_m IN NUMBER,
    p_number_tracks IN NUMBER,
    p_siding_position IN NUMBER DEFAULT NULL,
    p_siding_length IN NUMBER DEFAULT NULL
) RETURN VARCHAR2
IS
    v_owner_count NUMBER;
    v_start_facility_count NUMBER;
    v_end_facility_count NUMBER;
    v_gauge_count NUMBER;
    v_line_exists NUMBER;
    v_segment_exists NUMBER;
    v_result VARCHAR2(500);
BEGIN
    -- Validate that the line ID doesn't already exist
SELECT COUNT(*) INTO v_line_exists
FROM Line
WHERE id = p_line_id;

IF v_line_exists > 0 THEN
        RETURN 'ERROR: Line ID ' || p_line_id || ' already exists';
END IF;

    -- Validate that the segment ID doesn't already exist
SELECT COUNT(*) INTO v_segment_exists
FROM Segment
WHERE id = p_segment_id;

IF v_segment_exists > 0 THEN
        RETURN 'ERROR: Segment ID ' || p_segment_id || ' already exists';
END IF;

    -- Validate owner exists
SELECT COUNT(*) INTO v_owner_count
FROM Line_Owner
WHERE vat_number = p_owner_vat;

IF v_owner_count = 0 THEN
        RETURN 'ERROR: Owner with VAT ' || p_owner_vat || ' does not exist';
END IF;

    -- Validate start facility exists
SELECT COUNT(*) INTO v_start_facility_count
FROM Facility
WHERE id = p_start_facility_id;

IF v_start_facility_count = 0 THEN
        RETURN 'ERROR: Start facility with ID ' || p_start_facility_id || ' does not exist';
END IF;

    -- Validate end facility exists
SELECT COUNT(*) INTO v_end_facility_count
FROM Facility
WHERE id = p_end_facility_id;

IF v_end_facility_count = 0 THEN
        RETURN 'ERROR: End facility with ID ' || p_end_facility_id || ' does not exist';
END IF;

    -- Validate gauge exists
SELECT COUNT(*) INTO v_gauge_count
FROM Railway_Gauge
WHERE gauge_value = p_gauge;

IF v_gauge_count = 0 THEN
        RETURN 'ERROR: Gauge ' || p_gauge || ' does not exist in Railway_Gauge table';
END IF;

    -- Validate start and end facilities are different
    IF p_start_facility_id = p_end_facility_id THEN
        RETURN 'ERROR: Start and end facilities must be different';
END IF;

    -- Validate electrified value
    IF p_electrified NOT IN ('Yes', 'No') THEN
        RETURN 'ERROR: Electrified must be ''Yes'' or ''No''';
END IF;

    -- Validate segment parameters
    IF p_length_m <= 0 THEN
        RETURN 'ERROR: Segment length must be greater than 0';
END IF;

    IF p_max_weight_kg_m <= 0 THEN
        RETURN 'ERROR: Max weight per meter must be greater than 0';
END IF;

    IF p_number_tracks NOT IN (1, 2) THEN
        RETURN 'ERROR: Number of tracks must be 1 (single) or 2 (double)';
END IF;

    -- Validate siding parameters (if provided, both must be provided)
    IF (p_siding_position IS NOT NULL AND p_siding_length IS NULL) OR
       (p_siding_position IS NULL AND p_siding_length IS NOT NULL) THEN
        RETURN 'ERROR: If siding is specified, both position and length must be provided';
END IF;

    IF p_siding_position IS NOT NULL AND (p_siding_position < 0 OR p_siding_position > p_length_m) THEN
        RETURN 'ERROR: Siding position must be between 0 and segment length';
END IF;

    IF p_siding_length IS NOT NULL AND p_siding_length <= 0 THEN
        RETURN 'ERROR: Siding length must be greater than 0';
END IF;

    -- Insert the new line
INSERT INTO Line (id, name, owner_vat, start_facility_id, end_facility_id, gauge)
VALUES (p_line_id, p_line_name, p_owner_vat, p_start_facility_id, p_end_facility_id, p_gauge);

-- Insert the first segment (sequence_order = 1)
INSERT INTO Segment (
    id,
    line_id,
    sequence_order,
    electrified,
    max_weight_kg_m,
    length_m,
    number_tracks,
    siding_position,
    siding_length
) VALUES (
             p_segment_id,
             p_line_id,
             1,
             p_electrified,
             p_max_weight_kg_m,
             p_length_m,
             p_number_tracks,
             p_siding_position,
             p_siding_length
         );

COMMIT;

v_result := 'SUCCESS: Line ' || p_line_id || ' (' || p_line_name || ') created with segment ' || p_segment_id;
RETURN v_result;

EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
RETURN 'ERROR: ' || SQLERRM;
END add_new_line;
/