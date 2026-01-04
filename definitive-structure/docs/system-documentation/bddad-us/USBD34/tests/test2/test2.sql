SET SERVEROUTPUT ON;
DECLARE
v_freight_id NUMBER := 99999; -- ID que não existe
    v_train_id   NUMBER := 5435;
BEGIN
    DBMS_OUTPUT.PUT_LINE('--- TESTE 2: ID Inexistente ---');
    
    -- Tentar associar
    prc_associate_freight_to_train(v_freight_id, v_train_id);

EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('Resultado esperado (Erro): ' || SQLERRM);
END;
/