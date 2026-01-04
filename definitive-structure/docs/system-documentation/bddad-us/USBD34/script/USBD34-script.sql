CREATE OR REPLACE PROCEDURE prc_associate_freight_to_train(
    p_freight_id IN NUMBER,
    p_train_id   IN NUMBER
) IS
    v_freight_exists NUMBER;
    v_train_exists   NUMBER;
    v_already_linked NUMBER;
BEGIN
    -- 1. Validação: O Frete existe?
SELECT COUNT(*) INTO v_freight_exists FROM Freight WHERE id = p_freight_id;
IF v_freight_exists = 0 THEN
        RAISE_APPLICATION_ERROR(-20010, 'Erro: Frete com ID ' || p_freight_id || ' não encontrado.');
END IF;

    -- 2. Validação: O Comboio existe?
SELECT COUNT(*) INTO v_train_exists FROM Train WHERE id = p_train_id;
IF v_train_exists = 0 THEN
        RAISE_APPLICATION_ERROR(-20011, 'Erro: Comboio com ID ' || p_train_id || ' não encontrado.');
END IF;

    -- 3. Validação: O frete já está associado a este ou outro comboio?
    -- (Regra de negócio: um frete costuma estar em apenas um comboio de cada vez)
SELECT COUNT(*) INTO v_already_linked FROM Train_Freight WHERE freight_id = p_freight_id;
IF v_already_linked > 0 THEN
        RAISE_APPLICATION_ERROR(-20012, 'Erro: O frete ' || p_freight_id || ' já está associado a um comboio.');
END IF;

    -- 4. Inserção na tabela de associação
    -- O Trigger 'trg_check_length_freight' (do USBD33) será disparado aqui.
    -- Se o comprimento exceder o limite, o trigger lançará um erro e o INSERT falhará.
INSERT INTO Train_Freight (train_id, freight_id)
VALUES (p_train_id, p_freight_id);

DBMS_OUTPUT.PUT_LINE('Sucesso: Frete ' || p_freight_id || ' associado ao comboio ' || p_train_id);

EXCEPTION
    WHEN OTHERS THEN
        -- Re-lança o erro (incluindo os erros do trigger de comprimento)
        RAISE;
END;
