SET SERVEROUTPUT ON;
DECLARE
v_freight_id NUMBER := 9003;
    v_origin     NUMBER := 50;
    v_model      NUMBER := 1104;
    v_qty        NUMBER := 1000; -- Exagerado
BEGIN
    DBMS_OUTPUT.PUT_LINE('--- USBD40: TESTE 3 (Falha - Quantidade Exagerada) ---');

    pr_register_freight(
        p_freight_id => v_freight_id,
        p_origin_id  => v_origin,
        p_dest_id    => 11,
        p_date       => SYSDATE + 2,
        p_model_id   => v_model,
        p_qty        => v_qty
    );

    DBMS_OUTPUT.PUT_LINE('RESULTADO: FALHA. O sistema permitiu pedir stock inexistente.');
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE = -20012 THEN
            DBMS_OUTPUT.PUT_LINE('RESULTADO: SUCESSO. Erro de quantidade insuficiente detetado.');
ELSE
            DBMS_OUTPUT.PUT_LINE('RESULTADO: FALHA. Erro inesperado: ' || SQLERRM);
END IF;
END;
/