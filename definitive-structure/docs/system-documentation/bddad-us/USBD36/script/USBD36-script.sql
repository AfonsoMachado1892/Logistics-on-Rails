/* ==========================================================================
   IMPLEMENTAÇÃO USBD36: Adicionar Edifício (Building) a uma Facility
   ========================================================================== */

CREATE OR REPLACE PROCEDURE pr_add_building_to_facility(
    p_building_id IN NUMBER,
    p_facility_id IN NUMBER,
    p_name        IN VARCHAR2,
    p_type        IN VARCHAR2
) IS
    v_facility_count NUMBER;
    v_id_count       NUMBER;
BEGIN
    -- 1. Validação de parâmetros obrigatórios
    IF p_building_id IS NULL OR p_facility_id IS NULL OR p_name IS NULL OR p_type IS NULL THEN
        RAISE_APPLICATION_ERROR(-20003, 'USBD36: Building ID, Facility ID, Nome e Tipo são obrigatórios.');
END IF;

    -- 2. Verificar se o Building ID já existe
SELECT COUNT(*) INTO v_id_count FROM Building WHERE id = p_building_id;
IF v_id_count > 0 THEN
        RAISE_APPLICATION_ERROR(-20005, 'USBD36: Building ID ' || p_building_id || ' já existe.');
END IF;

    -- 3. Verificar se a Facility (Estação) existe
SELECT COUNT(*)
INTO v_facility_count
FROM Facility
WHERE id = p_facility_id;

IF v_facility_count = 0 THEN
        RAISE_APPLICATION_ERROR(-20004, 'USBD36: Estação com ID ' || p_facility_id || ' não encontrada.');
END IF;

    -- 4. Inserção do Edifício
INSERT INTO Building (id, facility_id, name, type)
VALUES (p_building_id, p_facility_id, p_name, p_type);

-- 5. Feedback e Commit
DBMS_OUTPUT.PUT_LINE('Sucesso: Edifício "' || p_name || '" (ID: ' || p_building_id || ') adicionado à estação ' || p_facility_id || '.');
COMMIT;
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE; -- Propagar o erro original
END;
/