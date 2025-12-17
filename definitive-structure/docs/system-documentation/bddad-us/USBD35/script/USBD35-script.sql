CREATE OR REPLACE FUNCTION fn_register_facility(
    p_name IN VARCHAR2
) RETURN NUMBER IS
    v_count          NUMBER;
    v_new_id         NUMBER;
    v_clean_name     VARCHAR2(100);
    v_normalized_in  VARCHAR2(100);
BEGIN
    -- 1. Validate Input (FIXED)
    -- In Oracle, TRIM('   ') returns NULL. We must check IS NULL.
    IF p_name IS NULL OR TRIM(p_name) IS NULL THEN
        RAISE_APPLICATION_ERROR(-20001, 'USBD35: Facility name cannot be null or empty.');
END IF;

    -- 2. Normalize Input
    -- '   Viana   do   Castelo  ' becomes 'Viana do Castelo'
    v_clean_name := REGEXP_REPLACE(TRIM(p_name), '\s+', ' ');

    -- Create upper-case version strictly for duplicate detection
    v_normalized_in := UPPER(v_clean_name);

    -- 3. Check for duplicates
SELECT COUNT(*)
INTO v_count
FROM Facility
WHERE UPPER(REGEXP_REPLACE(TRIM(name), '\s+', ' ')) = v_normalized_in;

IF v_count > 0 THEN
        RAISE_APPLICATION_ERROR(-20002, 'USBD35: Facility "' || v_clean_name || '" already exists (Duplicate name detected).');
END IF;

    -- 4. Generate New ID
    v_new_id := seq_facility_id.NEXTVAL;

    -- 5. Insert the new Facility
INSERT INTO Facility (id, name)
VALUES (v_new_id, v_clean_name);

-- 6. Commit and Return
COMMIT;
RETURN v_new_id;

EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE;
END;
/