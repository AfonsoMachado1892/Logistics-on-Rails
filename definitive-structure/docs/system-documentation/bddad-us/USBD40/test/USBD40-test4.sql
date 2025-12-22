DECLARE
v_new_id NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('--- TESTE 4: Stock Insuficiente Parcial (Pedir mais do que existe) ---');

    -- Cenário: Vamos assumir que em Leixões (50) existem vagões do modelo 1104.
    -- Vamos pedir uma quantidade absurda (ex: 1000) que sabemos ser superior ao stock real.
    -- Isto testa se a lógica "IF v_available < p_qty" funciona para valores > 0.

BEGIN
        pr_register_freight(
            p_origin_id => 50,  -- Leixões (Tem alguns vagões)
            p_dest_id   => 11,
            p_date      => SYSDATE + 5,
            p_model_id  => 1104,
            p_qty       => 1000, -- Quantidade exagerada
            p_new_id    => v_new_id
        );

        DBMS_OUTPUT.PUT_LINE('RESULTADO: FALHA. O sistema permitiu pedir mais vagões do que os existentes.');

EXCEPTION
        WHEN OTHERS THEN
            -- Espera-se o erro -20041 (Stock insuficiente)
            IF SQLCODE = -20041 THEN
                DBMS_OUTPUT.PUT_LINE('RESULTADO: SUCESSO. O sistema detetou que 1000 > stock disponível (Erro -20041).');
ELSE
                DBMS_OUTPUT.PUT_LINE('RESULTADO: FALHA. Erro incorreto: ' || SQLERRM);
END IF;
END;
END;
/