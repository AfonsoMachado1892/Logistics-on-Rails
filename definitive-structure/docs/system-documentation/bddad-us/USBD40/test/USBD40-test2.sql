SET SERVEROUTPUT ON;
DECLARE
v_freight_id NUMBER := 9002;
    v_origin     NUMBER := 11;   -- Valença (Sem stock)
    v_model      NUMBER := 1104;
BEGIN
    DBMS_OUTPUT.PUT_LINE('--- USBD40: TESTE 2 (Falha - Sem Stock na Estação) ---');

    pr_register_freight(
        p_freight_id => v_freight_id,
        p_origin_id  => v_origin,
        p_dest_id    => 50,
        p_date       => SYSDATE + 2,
        p_model_id   => v_model,
        p_qty        => 1
    );

    DBMS_OUTPUT.PUT_LINE('RESULTADO: FALHA. O sistema permitiu criar sem vagões.');
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE = -20012 THEN
            DBMS_OUTPUT.PUT_LINE('RESULTADO: SUCESSO. Erro de falta de vagões detetado.');
ELSE
            DBMS_OUTPUT.PUT_LINE('RESULTADO: FALHA. Erro inesperado: ' || SQLERRM);
END IF;
END;
/