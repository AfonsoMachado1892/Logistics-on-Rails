CREATE OR REPLACE FUNCTION fn_register_facility(
    p_facility_id IN NUMBER,
    p_name        IN VARCHAR2
) RETURN NUMBER IS
    v_clean_name     VARCHAR2(100);
    v_normalized_in  VARCHAR2(100);
    v_count          NUMBER;
BEGIN
    -- 1. Validate Input: Check for NULLs
    IF p_facility_id IS NULL THEN
        RAISE_APPLICATION_ERROR(-20001, 'Facility ID cannot be null.');
END IF;

    IF p_name IS NULL OR TRIM(p_name) IS NULL THEN
        RAISE_APPLICATION_ERROR(-20002, 'Facility name cannot be null or empty.');
END IF;

    -- 2. Check if Facility ID already exists
SELECT COUNT(*) INTO v_count FROM Facility WHERE id = p_facility_id;
IF v_count > 0 THEN
        RAISE_APPLICATION_ERROR(-20003, 'Facility ID ' || p_facility_id || ' already exists.');
END IF;

    -- 3. Normalize Input: Trim and collapse multiple spaces
    v_clean_name := REGEXP_REPLACE(TRIM(p_name), '\s+', ' ');
    v_normalized_in := UPPER(v_clean_name);

    -- 4. Case and Space Insensitive Duplicate Check (Name)
SELECT COUNT(*)
INTO v_count
FROM Facility
WHERE UPPER(REGEXP_REPLACE(TRIM(name), '\s+', ' ')) = v_normalized_in;

IF v_count > 0 THEN
        RAISE_APPLICATION_ERROR(-20004, 'Facility already exists (case/space insensitive check).');
END IF;

    -- 5. Insert new Facility using the provided ID
INSERT INTO Facility (id, name)
VALUES (p_facility_id, v_clean_name);

-- 6. TRANSAÇÃO: Sucess
COMMIT;
-- Return the ID that was just inserted
RETURN p_facility_id;

EXCEPTION
    WHEN OTHERS THEN
        -- 7. TRANSAÇÃO: Erro
        ROLLBACK;
        RAISE;
END;
/