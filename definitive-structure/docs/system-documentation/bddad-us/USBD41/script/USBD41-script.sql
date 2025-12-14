/* ==========================================================================
   IMPLEMENTAÇÃO USBD41: Remover Freight de um Comboio
   ========================================================================== */
CREATE OR REPLACE PROCEDURE pr_remove_freight_from_train(
    p_train_id   IN NUMBER,
    p_freight_id IN NUMBER
) IS
    v_count NUMBER;
BEGIN
    -- 1. Validação de parâmetros obrigatórios
    IF p_train_id IS NULL OR p_freight_id IS NULL THEN
        RAISE_APPLICATION_ERROR(-20010, 'USBD41: Train ID e Freight ID são obrigatórios.');
END IF;

    -- 2. Verificar se a associação existe na tabela Train_Freight
SELECT COUNT(*)
INTO v_count
FROM Train_Freight
WHERE train_id = p_train_id AND freight_id = p_freight_id;

IF v_count = 0 THEN
        RAISE_APPLICATION_ERROR(-20011, 'USBD41: O Freight ' || p_freight_id ||
                                        ' não está associado ao Comboio ' || p_train_id ||
                                        ' (ou os IDs não existem).');
END IF;

    -- 3. Remover a associação
DELETE FROM Train_Freight
WHERE train_id = p_train_id AND freight_id = p_freight_id;

-- 4. Feedback e Commit
DBMS_OUTPUT.PUT_LINE('Ação Realizada: Freight ' || p_freight_id || ' removido do Comboio ' || p_train_id || '.');
COMMIT;

EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        -- Relança o erro para ser capturado nos testes
        RAISE;
END;
/