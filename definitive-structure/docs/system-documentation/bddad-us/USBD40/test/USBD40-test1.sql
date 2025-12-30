SET SERVEROUTPUT ON;
DECLARE
v_freight_id NUMBER := 9001; -- ID Novo manual
    v_origin     NUMBER := 50;   -- Leixões (Tem stock)
    v_dest       NUMBER := 11;   -- Valença
    v_model      NUMBER := 1104; -- Modelo existente
    v_qty        NUMBER := 1;    -- Pedir 1 vagão

    v_check_f    NUMBER;
    v_check_w    NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('--- USBD40: TESTE 1 (Sucesso) ---');

    -- Limpeza preventiva
DELETE FROM Freight_Wagon WHERE freight_id = v_freight_id;
DELETE FROM Freight WHERE id = v_freight_id;

-- Executar Procedure
pr_register_freight(
        p_freight_id => v_freight_id,
        p_origin_id  => v_origin,
        p_dest_id    => v_dest,
        p_date       => SYSDATE + 2,
        p_model_id   => v_model,
        p_qty        => v_qty
    );

    -- Validação
SELECT COUNT(*) INTO v_check_f FROM Freight WHERE id = v_freight_id;
SELECT COUNT(*) INTO v_check_w FROM Freight_Wagon WHERE freight_id = v_freight_id;

IF v_check_f = 1 AND v_check_w = v_qty THEN
        DBMS_OUTPUT.PUT_LINE('RESULTADO: SUCESSO. Freight criado e vagões associados.');
ELSE
        DBMS_OUTPUT.PUT_LINE('RESULTADO: FALHA. Contagem incorreta.');
END IF;

ROLLBACK;
END;
/