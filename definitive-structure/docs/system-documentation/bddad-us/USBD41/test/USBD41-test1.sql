DECLARE
v_check NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('--- TESTE 1: Remover Freight 2001 do Comboio 5421 (Cenário de Sucesso) ---');

    -- 1. Executar a ação
    pr_remove_freight_from_train(5421, 2001);

    -- 2. Validar o resultado (A contagem deve ser 0 agora)
SELECT COUNT(*) INTO v_check
FROM Train_Freight
WHERE train_id = 5421 AND freight_id = 2001;

IF v_check = 0 THEN
        DBMS_OUTPUT.PUT_LINE('RESULTADO: SUCESSO. A ligação foi eliminada da base de dados.');
ELSE
        DBMS_OUTPUT.PUT_LINE('RESULTADO: FALHA. A ligação ainda existe.');
END IF;
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('RESULTADO: FALHA. Ocorreu um erro inesperado: ' || SQLERRM);
END;
/