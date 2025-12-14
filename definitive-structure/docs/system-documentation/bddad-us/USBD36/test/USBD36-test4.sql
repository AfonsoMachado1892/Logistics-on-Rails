BEGIN
    DBMS_OUTPUT.PUT_LINE('--- TESTE 4: Tentar inserir com Nome NULL ---');

BEGIN
        pr_add_building_to_facility(50, NULL, 'Terminal');

        DBMS_OUTPUT.PUT_LINE('RESULTADO: FALHA. O sistema aceitou um nome NULL.');

EXCEPTION
        WHEN OTHERS THEN
            -- Validar se é o erro de parâmetros (-20003)
            IF SQLCODE = -20003 THEN
                DBMS_OUTPUT.PUT_LINE('RESULTADO: SUCESSO. O sistema bloqueou dados nulos conforme esperado.');
ELSE
                DBMS_OUTPUT.PUT_LINE('RESULTADO: FALHA. Erro incorreto: ' || SQLERRM);
END IF;
END;
END;
/