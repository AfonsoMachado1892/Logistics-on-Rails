-- Teste 5: Validação rigorosa de normalização de nomes
SET SERVEROUTPUT ON;

DECLARE
v_result NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('========================================');
    DBMS_OUTPUT.PUT_LINE('TESTE 5: VALIDAÇÃO DE NORMALIZAÇÃO');
    DBMS_OUTPUT.PUT_LINE('========================================');
    DBMS_OUTPUT.PUT_LINE('');

    -- Setup: Limpeza de dados para o teste 5
DELETE FROM Railway_Gauge WHERE gauge_value IN (5000, 5001, 5002);
COMMIT;

-- Inserção base para comparação
v_result := insert_gauge('Bitola Europeia', 5000);
    DBMS_OUTPUT.PUT_LINE('Inserção base (5000mm): ' || v_result || ' (Esperado: 1)');
    DBMS_OUTPUT.PUT_LINE('');

    -- Teste 5a: Duplicado com muitos espaços internos
    DBMS_OUTPUT.PUT_LINE('Teste 5a: Nome com múltiplos espaços (Bitola    Europeia)');
    v_result := insert_gauge('Bitola    Europeia', 5001);
    DBMS_OUTPUT.PUT_LINE('Resultado: ' || v_result || ' (Esperado: -3)');
    IF v_result = -3 THEN
        DBMS_OUTPUT.PUT_LINE('✓ PASSOU');
ELSE
        DBMS_OUTPUT.PUT_LINE('✗ FALHOU');
END IF;
    DBMS_OUTPUT.PUT_LINE('');

    -- Teste 5b: Duplicado com Case trocado e espaços nos extremos
    DBMS_OUTPUT.PUT_LINE('Teste 5b: Nome sujo (  bitola EUROPEIA  )');
    v_result := insert_gauge('  bitola EUROPEIA  ', 5002);
    DBMS_OUTPUT.PUT_LINE('Resultado: ' || v_result || ' (Esperado: -3)');
    IF v_result = -3 THEN
        DBMS_OUTPUT.PUT_LINE('✓ PASSOU');
ELSE
        DBMS_OUTPUT.PUT_LINE('✗ FALHOU');
END IF;

    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('========================================');
END;
/

-- Listar para confirmar que os duplicados não foram inseridos
SELECT gauge_value, description FROM Railway_Gauge WHERE gauge_value BETWEEN 5000 AND 5002;