DECLARE
v_link_count NUMBER;
    v_freight_exists NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('--- TESTE 5: Verificação de Integridade (Não apagar o Freight) ---');

    -- 1. Remover a ligação (Freight 2002 do Comboio 5421)
    pr_remove_freight_from_train(5421, 2002);

    -- 2. Verificar se a ligação desapareceu
SELECT COUNT(*) INTO v_link_count
FROM Train_Freight
WHERE train_id = 5421 AND freight_id = 2002;

-- 3. Verificar se o Freight ID ainda existe na tabela 'Freight'
SELECT COUNT(*) INTO v_freight_exists
FROM Freight
WHERE id = 2002;

-- Lógica do Teste
IF v_link_count = 0 AND v_freight_exists = 1 THEN
        DBMS_OUTPUT.PUT_LINE('RESULTADO: SUCESSO. O Freight foi desassociado do comboio, mas o registo da carga mantém-se no sistema.');
ELSE
        DBMS_OUTPUT.PUT_LINE('RESULTADO: FALHA. Ou a ligação não foi apagada, ou apagaste o Freight inadvertidamente.');
END IF;
END;
/