BEGIN
    DBMS_OUTPUT.PUT_LINE('--- TESTE 3: Tentar remover IDs inexistentes ---');

BEGIN
        pr_remove_freight_from_train(9999, 8888);

        DBMS_OUTPUT.PUT_LINE('RESULTADO: FALHA. O sistema aceitou IDs inventados.');

EXCEPTION
        WHEN OTHERS THEN
            IF SQLCODE = -20011 THEN
                DBMS_OUTPUT.PUT_LINE('RESULTADO: SUCESSO. Erro -20011 disparado corretamente para dados inexistentes.');
ELSE
                DBMS_OUTPUT.PUT_LINE('RESULTADO: FALHA. Erro incorreto: ' || SQLERRM);
END IF;
END;
END;
/