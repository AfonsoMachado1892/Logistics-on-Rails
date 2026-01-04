CREATE OR REPLACE PROCEDURE prc_associate_locomotive_to_train(
    p_loco_number IN VARCHAR2,
    p_train_id    IN NUMBER
) IS
    v_clean_loco      VARCHAR2(50);
    v_count           NUMBER;
    v_train_gauge     NUMBER;
    v_loco_gauge      NUMBER;
BEGIN
    -- 1. Normalização (Case & Space Insensitive)
    v_clean_loco := UPPER(REGEXP_REPLACE(TRIM(p_loco_number), '\s+', ' '));

    -- 2. Verificar existência do comboio
SELECT COUNT(*) INTO v_count FROM Train WHERE id = p_train_id;
IF v_count = 0 THEN
        RAISE_APPLICATION_ERROR(-20301, 'Erro: Comboio ' || p_train_id || ' não encontrado.');
END IF;

    -- 3. Verificar existência da locomotiva
SELECT COUNT(*) INTO v_count FROM Locomotive WHERE UPPER(loco_number) = v_clean_loco;
IF v_count = 0 THEN
        RAISE_APPLICATION_ERROR(-20302, 'Erro: Locomotiva ' || v_clean_loco || ' não encontrada.');
END IF;

    -- 4. Verificação de Bitola Única (Gauge)
    -- Obtém a bitola da locomotiva através do modelo
SELECT lm.gauge INTO v_loco_gauge
FROM Locomotive l
         JOIN Locomotive_Model lm ON l.model_id = lm.id
WHERE UPPER(l.loco_number) = v_clean_loco;

-- Obtém a bitola da linha onde o comboio opera (baseado na Facility de partida)
BEGIN
SELECT gauge INTO v_train_gauge
FROM Line
WHERE start_facility_id = (SELECT start_facility_id FROM Train WHERE id = p_train_id)
  AND ROWNUM = 1;
EXCEPTION WHEN NO_DATA_FOUND THEN
        -- Caso não haja linha mapeada, impede a associação por segurança
        RAISE_APPLICATION_ERROR(-20304, 'Erro: Não foi possível determinar a bitola da linha para este comboio.');
END;

    -- Comparação de Bitolas
    IF v_train_gauge != v_loco_gauge THEN
        RAISE_APPLICATION_ERROR(-20303, 'Erro: Bitola incompatível. Linha: ' || v_train_gauge || 'mm vs Loco: ' || v_loco_gauge || 'mm');
END IF;

    -- 5. Inserção (O Trigger trg_check_length_loco da USBD33 validará o comprimento aqui)
INSERT INTO Train_Locomotive (train_id, loco_number)
VALUES (p_train_id, v_clean_loco);

DBMS_OUTPUT.PUT_LINE('Sucesso: Locomotiva ' || v_clean_loco || ' associada ao comboio ' || p_train_id);
END;
/