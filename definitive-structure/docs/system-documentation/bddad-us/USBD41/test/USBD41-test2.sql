BEGIN
    DBMS_OUTPUT.PUT_LINE('--- TESTE 2: Tentar remover Freight que está noutro comboio ---');

BEGIN
        -- Tenta remover o Freight 2006 (que pertence ao comboio 5435) do comboio 5421
        pr_remove_freight_from_train(5421, 2006);

        -- Se chegar aqui, falhou
        DBMS_OUTPUT.PUT_LINE('RESULTADO: FALHA. O sistema permitiu remover uma ligação que não existia.');

EXCEPTION
        WHEN OTHERS THEN
            -- Esperamos o erro -20011
            IF SQLCODE = -20011 THEN
                DBMS_OUTPUT.PUT_LINE('RESULTADO: SUCESSO. O sistema detetou que a associação não existe (Erro -20011).');
ELSE
                DBMS_OUTPUT.PUT_LINE('RESULTADO: FALHA. Erro incorreto: ' || SQLERRM);
END IF;
END;
END;
/