SET SERVEROUTPUT ON;
BEGIN
    DBMS_OUTPUT.PUT_LINE('--- TESTE 3: Tentar inserir em estação inexistente (ID 9999) ---');

BEGIN
        -- Manual ID 1003
        pr_add_building_to_facility(1003, 9999, 'Oficina Fantasma', 'Maintenance');

        -- Se chegar aqui, falhou, porque devia ter dado erro antes!
        DBMS_OUTPUT.PUT_LINE('RESULTADO: FALHA. O sistema permitiu inserir numa estação que não existe.');

EXCEPTION
        WHEN OTHERS THEN
            -- Validar se é o nosso erro personalizado (-20004)
            IF SQLCODE = -20004 THEN
                DBMS_OUTPUT.PUT_LINE('RESULTADO: SUCESSO. O sistema bloqueou com o erro esperado (-20004).');
ELSE
                DBMS_OUTPUT.PUT_LINE('RESULTADO: FALHA. Deu um erro inesperado: ' || SQLERRM);
END IF;
END;
END;
/