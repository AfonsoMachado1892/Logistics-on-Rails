-- Teste 1: Inserção bem-sucedida - Bitola Métrica
SET SERVEROUTPUT ON;

-- Limpar dados do teste anterior (garantir ambiente limpo)
DELETE FROM Railway_Gauge WHERE gauge_value = 1000;
DELETE FROM Railway_Gauge WHERE UPPER(description) = 'BITOLA MÉTRICA';
COMMIT;

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

-- Verificar se foi inserida
SELECT * FROM Railway_Gauge WHERE gauge_value = 1000;