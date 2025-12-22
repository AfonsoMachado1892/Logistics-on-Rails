DECLARE
v_new_id NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('--- TESTE 3: Tentar Registar Freight com quantidade 0 ou negativa ---');

BEGIN
        pr_register_freight(
            p_origin_id => 50,
            p_dest_id   => 11,
            p_date      => SYSDATE + 5,
            p_model_id  => 1104,
            p_qty       => 0,    -- Quantidade Inválida
            p_new_id    => v_new_id
        );

        DBMS_OUTPUT.PUT_LINE('RESULTADO: FALHA. O sistema aceitou quantidade zero.');

EXCEPTION
        WHEN OTHERS THEN
            -- Espera-se o erro -20040 (Parâmetros inválidos)
            IF SQLCODE = -20040 THEN
                DBMS_OUTPUT.PUT_LINE('RESULTADO: SUCESSO. O sistema validou a quantidade (Erro -20040).');
ELSE
                DBMS_OUTPUT.PUT_LINE('RESULTADO: FALHA. Erro incorreto: ' || SQLERRM);
END IF;
END;
END;
/