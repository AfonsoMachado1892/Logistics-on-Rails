CREATE OR REPLACE FUNCTION fn_register_facility(
    p_name IN VARCHAR2
) RETURN NUMBER IS
    v_new_id         NUMBER;
    v_clean_name     VARCHAR2(100);
    v_normalized_in  VARCHAR2(100);
    v_count          NUMBER;
BEGIN
    -- 1. Validate Input: Check for NULL or whitespace
    IF p_name IS NULL OR TRIM(p_name) IS NULL THEN
        RAISE_APPLICATION_ERROR(-20001, 'Facility name cannot be null or empty.');
END IF;

    -- 2. Normalize Input: Trim and collapse multiple spaces
    v_clean_name := REGEXP_REPLACE(TRIM(p_name), '\s+', ' ');
    v_normalized_in := UPPER(v_clean_name);

    -- 3. Case and Space Insensitive Duplicate Check
SELECT COUNT(*)
INTO v_count
FROM Facility
WHERE UPPER(REGEXP_REPLACE(TRIM(name), '\s+', ' ')) = v_normalized_in;

IF v_count > 0 THEN
        RAISE_APPLICATION_ERROR(-20002, 'Facility already exists (case/space insensitive check).');
END IF;

    -- 4. Manual ID Generation (Forbidden to use SEQUENCES/IDENTITY)
SELECT NVL(MAX(id), 0) + 1 INTO v_new_id FROM Facility;

-- 5. Insert new Facility
INSERT INTO Facility (id, name)
VALUES (v_new_id, v_clean_name);

-- 6. Commit Transaction
COMMIT;

RETURN v_new_id;

EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE;
END;
/