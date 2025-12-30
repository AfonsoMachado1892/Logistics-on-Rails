SET SERVEROUTPUT ON;
DECLARE
v_existing_id NUMBER := 2001; -- Já existe
BEGIN
    DBMS_OUTPUT.PUT_LINE('--- USBD40: TESTE 4 (Falha - ID Duplicado) ---');

    pr_register_freight(
        p_freight_id => v_existing_id,
        p_origin_id  => 50,
        p_dest_id    => 11,
        p_date       => SYSDATE + 2,
        p_model_id   => 1104,
        p_qty        => 1
    );

    DBMS_OUTPUT.PUT_LINE('RESULTADO: FALHA. O sistema permitiu ID duplicado.');
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE = -20011 THEN
            DBMS_OUTPUT.PUT_LINE('RESULTADO: SUCESSO. Erro de ID duplicado detetado.');
ELSE
            DBMS_OUTPUT.PUT_LINE('RESULTADO: FALHA. Erro inesperado: ' || SQLERRM);
END IF;
END;
/