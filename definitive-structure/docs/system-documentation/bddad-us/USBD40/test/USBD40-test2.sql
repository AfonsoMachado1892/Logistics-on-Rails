DECLARE
v_new_id NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('--- TESTE 2: Tentar Registar Freight sem Vagões na Estação ---');

BEGIN
        -- Tentar tirar vagões da estação 1 (São Romão).
        -- Como o script inicial (DDL) meteu todos os vagões na estação 50, a estação 1 tem 0 vagões.
        pr_register_freight(
            p_origin_id => 1,   -- Origem: São Romão (Sem vagões)
            p_dest_id   => 11,
            p_date      => SYSDATE + 5,
            p_model_id  => 1104,
            p_qty       => 1,
            p_new_id    => v_new_id
        );

        -- Se chegar aqui, falhou
        DBMS_OUTPUT.PUT_LINE('RESULTADO: FALHA. O sistema permitiu criar freight sem vagões disponíveis.');

EXCEPTION
        WHEN OTHERS THEN
            -- Espera-se o erro -20041 (Stock insuficiente)
            IF SQLCODE = -20041 THEN
                DBMS_OUTPUT.PUT_LINE('RESULTADO: SUCESSO. O sistema detetou falta de vagões na origem (Erro -20041).');
ELSE
                DBMS_OUTPUT.PUT_LINE('RESULTADO: FALHA. Erro incorreto: ' || SQLERRM);
END IF;
END;
END;
/