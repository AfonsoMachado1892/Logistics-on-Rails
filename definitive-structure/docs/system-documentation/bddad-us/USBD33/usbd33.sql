/* ==========================================================================
   IMPLEMENTAÇÃO USBD33: Validação de Comprimento do Comboio
   ========================================================================== */

-- 1. Função para calcular o comprimento atual (Locomotivas + Vagões)
CREATE OR REPLACE FUNCTION fn_calculate_train_length(p_train_id IN NUMBER)
RETURN NUMBER IS
    v_total_length_m NUMBER := 0;
    v_loco_len NUMBER := 0;
    v_wagons_len NUMBER := 0;
BEGIN
    -- Comprimento das Locomotivas (já em metros) - UPDATED to join Model
SELECT NVL(SUM(lm.length_m), 0)
INTO v_loco_len
FROM Train_Locomotive tl
         JOIN Locomotive l ON tl.loco_number = l.loco_number
         JOIN Locomotive_Model lm ON l.model_id = lm.id
WHERE tl.train_id = p_train_id;

-- Comprimento dos Vagões (converter mm para metros)
SELECT NVL(SUM(wm.length_mm / 1000), 0)
INTO v_wagons_len
FROM Train_Freight tf
         JOIN Freight f ON tf.freight_id = f.id
         JOIN Freight_Wagon fw ON f.id = fw.freight_id
         JOIN Wagon w ON fw.wagon_number = w.wagon_number
         JOIN Wagon_Model wm ON w.model_id = wm.id
WHERE tf.train_id = p_train_id;

v_total_length_m := v_loco_len + v_wagons_len;
RETURN v_total_length_m;
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        RETURN 0;
END;
/

-- 2. Trigger para verificar adição de Locomotivas
CREATE OR REPLACE TRIGGER trg_check_length_loco
BEFORE INSERT ON Train_Locomotive
FOR EACH ROW
DECLARE
v_max_len NUMBER;
    v_current_len NUMBER;
    v_new_loco_len NUMBER;
BEGIN
SELECT max_length_m INTO v_max_len FROM Train WHERE id = :NEW.train_id;

IF v_max_len IS NOT NULL THEN
        v_current_len := fn_calculate_train_length(:NEW.train_id);

-- UPDATED: Get length from Locomotive_Model via Locomotive
SELECT lm.length_m INTO v_new_loco_len
FROM Locomotive l
         JOIN Locomotive_Model lm ON l.model_id = lm.id
WHERE l.loco_number = :NEW.loco_number;

IF (v_current_len + v_new_loco_len) > v_max_len THEN
            RAISE_APPLICATION_ERROR(-20001, 'USBD33: Train length limit exceeded adding Loco. Max: '
                || v_max_len || 'm');
END IF;
END IF;
END;
/

-- 3. Trigger para verificar adição de Cargas (Freights)
CREATE OR REPLACE TRIGGER trg_check_length_freight
BEFORE INSERT ON Train_Freight
FOR EACH ROW
DECLARE
v_max_len NUMBER;
    v_current_len NUMBER;
    v_new_freight_len NUMBER;
BEGIN
SELECT max_length_m INTO v_max_len FROM Train WHERE id = :NEW.train_id;

IF v_max_len IS NOT NULL THEN
        v_current_len := fn_calculate_train_length(:NEW.train_id);

SELECT NVL(SUM(wm.length_mm / 1000), 0)
INTO v_new_freight_len
FROM Freight_Wagon fw
         JOIN Wagon w ON fw.wagon_number = w.wagon_number
         JOIN Wagon_Model wm ON w.model_id = wm.id
WHERE fw.freight_id = :NEW.freight_id;

IF (v_current_len + v_new_freight_len) > v_max_len THEN
            RAISE_APPLICATION_ERROR(-20002, 'USBD33: Train length limit exceeded adding Freight. Max: '
                || v_max_len || 'm');
END IF;
END IF;
END;
/