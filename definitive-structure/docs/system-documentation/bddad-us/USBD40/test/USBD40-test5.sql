DECLARE
v_new_id NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('--- TESTE 5: Integridade Referencial (Estação de Destino Inexistente) ---');

BEGIN
        -- Tentar enviar para a estação ID 99999 (que não existe)
        pr_register_freight(
            p_origin_id => 50,
            p_dest_id   => 99999, -- ID Inválido
            p_date      => SYSDATE + 5,
            p_model_id  => 1104,
            p_qty       => 1,
            p_new_id    => v_new_id
        );

        DBMS_OUTPUT.PUT_LINE('RESULTADO: FALHA. O sistema aceitou uma estação de destino inexistente.');

EXCEPTION
        WHEN OTHERS THEN
            -- O erro de Foreign Key no Oracle é geralmente ORA-02291.
            -- o SQLCODE será o erro original do Oracle.
            IF SQLCODE = -2291 THEN
                DBMS_OUTPUT.PUT_LINE('RESULTADO: SUCESSO. O sistema detetou violação de chave estrangeira (ORA-02291).');
ELSE
                -- Aceita-se qualquer erro que não seja sucesso, mas o ideal é o 2291.
                DBMS_OUTPUT.PUT_LINE('RESULTADO: SUCESSO (Parcial). Erro detetado: ' || SQLERRM);
END IF;
END;
END;
/