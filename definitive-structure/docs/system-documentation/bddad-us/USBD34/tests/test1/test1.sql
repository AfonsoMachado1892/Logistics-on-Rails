
DECLARE
v_freight_id NUMBER := 2007;
    v_train_id   NUMBER := 5435;
BEGIN
    DBMS_OUTPUT.PUT_LINE('--- TESTE 1: Sucesso ---');

    -- Executar a associação
    prc_associate_freight_to_train(v_freight_id, v_train_id);

    -- Verificar se foi inserido
COMMIT;
DBMS_OUTPUT.PUT_LINE('Sucesso: Frete ' || v_freight_id || ' associado com êxito.');
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        DBMS_OUTPUT.PUT_LINE('Erro inesperado: ' || SQLERRM);
END;
