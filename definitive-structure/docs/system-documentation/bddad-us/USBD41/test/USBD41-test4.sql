BEGIN
    DBMS_OUTPUT.PUT_LINE('--- TESTE 4: Tentar executar com parâmetros NULL ---');

BEGIN
        pr_remove_freight_from_train(NULL, 2002);

        DBMS_OUTPUT.PUT_LINE('RESULTADO: FALHA. O sistema aceitou parâmetros nulos.');

EXCEPTION
        WHEN OTHERS THEN
            -- Esperamos o erro -20010 definido no início do procedimento
            IF SQLCODE = -20010 THEN
                DBMS_OUTPUT.PUT_LINE('RESULTADO: SUCESSO. O sistema bloqueou o input NULL (Erro -20010).');
ELSE
                DBMS_OUTPUT.PUT_LINE('RESULTADO: FALHA. Erro incorreto: ' || SQLERRM);
END IF;
END;
END;
/