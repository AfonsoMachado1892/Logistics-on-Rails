
DECLARE
v_freight_id NUMBER := 2001;
    v_train_id   NUMBER := 5437;
BEGIN
    DBMS_OUTPUT.PUT_LINE('--- TESTE 3: Limite de Comprimento ---');

    -- Forçar um limite irrealista para o teste falhar
UPDATE Train SET max_length_m = 10 WHERE id = v_train_id;

-- Tentar associar (deve disparar o trigger trg_check_length_freight)
prc_associate_freight_to_train(v_freight_id, v_train_id);

ROLLBACK;
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        DBMS_OUTPUT.PUT_LINE('Resultado esperado (Erro): ' || SQLERRM);
END;
