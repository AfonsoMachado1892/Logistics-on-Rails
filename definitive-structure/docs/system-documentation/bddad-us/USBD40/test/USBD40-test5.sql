SET SERVEROUTPUT ON;
DECLARE
v_freight_id NUMBER := 9999;
BEGIN
    DBMS_OUTPUT.PUT_LINE('--- USBD40: TESTE 5 (Falha - Quantidade Negativa) ---');

    pr_register_freight(
        p_freight_id => v_freight_id,
        p_origin_id  => 50,
        p_dest_id    => 11,
        p_date       => SYSDATE + 2,
        p_model_id   => 1104,
        p_qty        => -5 -- Inválido
    );

    DBMS_OUTPUT.PUT_LINE('RESULTADO: FALHA. O sistema aceitou quantidade negativa.');
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE = -20010 THEN
            DBMS_OUTPUT.PUT_LINE('RESULTADO: SUCESSO. Erro de parâmetros detetado.');
ELSE
            DBMS_OUTPUT.PUT_LINE('RESULTADO: FALHA. Erro inesperado: ' || SQLERRM);
END IF;
END;
/