-- Teste 1: Inserção bem-sucedida - Bitola Métrica
SET SERVEROUTPUT ON;

DECLARE
v_result NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('Teste 1: Inserção bem-sucedida (Bitola Métrica, 1000mm)');
    v_result := insert_gauge('Bitola Métrica', 1000);
    DBMS_OUTPUT.PUT_LINE('Resultado: ' || v_result || ' (Esperado: 1)');

    IF v_result = 1 THEN
        DBMS_OUTPUT.PUT_LINE('✓ PASSOU');
ELSE
        DBMS_OUTPUT.PUT_LINE('✗ FALHOU');
END IF;
END;
/

SELECT * FROM Railway_Gauge WHERE gauge_value = 1000;