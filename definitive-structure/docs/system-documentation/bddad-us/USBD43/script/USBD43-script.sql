CREATE OR REPLACE FUNCTION fn_create_electric_loco_model(
    p_model_id      IN NUMBER,
    p_model_name    IN VARCHAR2,
    p_make          IN VARCHAR2,
    p_power_kw      IN NUMBER,
    p_weight_tons   IN NUMBER,
    p_max_speed_kmh IN NUMBER,
    p_traction_kn   IN NUMBER,
    p_voltage       IN VARCHAR2,
    p_frequency     IN VARCHAR2,
    p_gauge_desc    IN VARCHAR2,
    p_length_m      IN NUMBER,
    p_width_m       IN NUMBER,
    p_height_m      IN NUMBER,
    p_num_bogies    IN NUMBER,
    p_bogies_type   IN VARCHAR2
) RETURN NUMBER IS
    v_count         NUMBER;
    v_gauge_val     NUMBER;

    -- Exceptions
    ex_duplicate_id    EXCEPTION;
    ex_duplicate_model EXCEPTION;
    ex_gauge_not_found EXCEPTION;
    ex_invalid_input   EXCEPTION;

BEGIN
    -- 1. Validate mandatory ID and Strings
    IF p_model_id IS NULL OR TRIM(p_model_name) IS NULL OR TRIM(p_make) IS NULL OR TRIM(p_gauge_desc) IS NULL THEN
        RAISE ex_invalid_input;
END IF;

    -- 2. Validate numeric inputs (Must be positive)
    IF p_power_kw <= 0 OR p_weight_tons <= 0 OR p_max_speed_kmh <= 0 OR
       p_traction_kn <= 0 OR p_length_m <= 0 THEN
        RAISE ex_invalid_input;
END IF;

    -- 3. Check if Model ID already exists
SELECT COUNT(*) INTO v_count FROM Locomotive_Model WHERE id = p_model_id;
IF v_count > 0 THEN
        RAISE ex_duplicate_id;
END IF;

    -- 4. Check for Duplicate Model Name (Case and Space Insensitive)
SELECT COUNT(*)
INTO v_count
FROM Locomotive_Model
WHERE UPPER(TRIM(REGEXP_REPLACE(model_name, '\s+', ' '))) =
      UPPER(TRIM(REGEXP_REPLACE(p_model_name, '\s+', ' ')));

IF v_count > 0 THEN
        RAISE ex_duplicate_model;
END IF;

    -- 5. Find the Gauge ID based on Description
BEGIN
SELECT gauge_value
INTO v_gauge_val
FROM Railway_Gauge
WHERE UPPER(TRIM(REGEXP_REPLACE(description, '\s+', ' '))) =
      UPPER(TRIM(REGEXP_REPLACE(p_gauge_desc, '\s+', ' ')));
EXCEPTION
        WHEN NO_DATA_FOUND THEN
            RAISE ex_gauge_not_found;
WHEN TOO_MANY_ROWS THEN
            RAISE ex_gauge_not_found;
END;

    -- 6. Insert the new Electric Model (Using provided ID)
INSERT INTO Locomotive_Model (
    id,
    model_name,
    make,
    power_kw,
    weight_tons,
    max_speed_kmh,
    traction_kn,
    engine_type,
    voltage,
    frequency,
    gauge,
    length_m,
    width_m,
    height_m,
    num_bogies,
    bogies_type
) VALUES (
             p_model_id,
             TRIM(p_model_name),
             TRIM(p_make),
             p_power_kw,
             p_weight_tons,
             p_max_speed_kmh,
             p_traction_kn,
             'Electric',
             TRIM(p_voltage),
             TRIM(p_frequency),
             v_gauge_val,
             p_length_m,
             p_width_m,
             p_height_m,
             p_num_bogies,
             TRIM(p_bogies_type)
         );

RETURN p_model_id;

EXCEPTION
    WHEN ex_duplicate_id THEN
        RAISE_APPLICATION_ERROR(-20004, 'Error: Model ID ' || p_model_id || ' already exists.');
WHEN ex_duplicate_model THEN
        RAISE_APPLICATION_ERROR(-20001, 'Error: Locomotive Model already exists: ' || p_model_name);
WHEN ex_gauge_not_found THEN
        RAISE_APPLICATION_ERROR(-20002, 'Error: Gauge not found: ' || p_gauge_desc);
WHEN ex_invalid_input THEN
        RAISE_APPLICATION_ERROR(-20003, 'Error: Invalid input parameters (NULLs or negative numbers).');
WHEN OTHERS THEN
        RAISE_APPLICATION_ERROR(-20999, 'Unexpected Error: ' || SQLERRM);
END;
/