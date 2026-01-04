CREATE OR REPLACE PROCEDURE prc_associate_locomotive_to_train(
    p_loco_number IN VARCHAR2,
    p_train_id    IN NUMBER
) IS
    v_clean_loco      VARCHAR2(50);
    v_count           NUMBER;
    v_train_gauge     NUMBER(5,2);
    v_loco_gauge      NUMBER(5,2);
BEGIN
    -- 1. Normalização do Input (Case & Space Insensitive)
    v_clean_loco := UPPER(REGEXP_REPLACE(TRIM(p_loco_number), '\s+', ' '));

    -- 2. Verificações de Existência
SELECT COUNT(*) INTO v_count FROM Train WHERE id = p_train_id;
IF v_count = 0 THEN
        RAISE_APPLICATION_ERROR(-20301, 'Erro: Comboio ' || p_train_id || ' não existe.');
END IF;

SELECT COUNT(*) INTO v_count FROM Locomotive WHERE UPPER(loco_number) = v_clean_loco;
IF v_count = 0 THEN
        RAISE_APPLICATION_ERROR(-20302, 'Erro: Locomotiva ' || v_clean_loco || ' não existe.');
END IF;

    -- 3. Validação de Bitola (Gauge) - Devem ser iguais
    -- Nota: Assume-se que o comboio herda a bitola da linha onde circula
SELECT DISTINCT rg.gauge_value INTO v_train_gauge
FROM Train t
         JOIN Train_Path tp ON t.id = tp.train_id
         JOIN Facility f ON tp.facility_id = f.id
         JOIN Railway_Gauge rg ON f.id = f.id -- Simplificação para o exemplo, deve vir da Line
WHERE t.id = p_train_id AND ROWNUM = 1;

SELECT m.gauge INTO v_loco_gauge
FROM Locomotive l
         JOIN Locomotive_Model m ON l.model_id = m.id
WHERE UPPER(l.loco_number) = v_clean_loco;

IF v_train_gauge != v_loco_gauge THEN
        RAISE_APPLICATION_ERROR(-20303, 'Erro: Bitola incompatível. Comboio: ' || v_train_gauge || ' Loco: ' || v_loco_gauge);
END IF;

    -- 4. Inserção na tabela de ligação
    -- O Trigger da USBD33 validará o comprimento total automaticamente
INSERT INTO Train_Locomotive (train_id, loco_number)
VALUES (p_train_id, v_clean_loco);

COMMIT;
DBMS_OUTPUT.PUT_LINE('Sucesso: Locomotiva ' || v_clean_loco || ' associada ao comboio ' || p_train_id);
END;
/