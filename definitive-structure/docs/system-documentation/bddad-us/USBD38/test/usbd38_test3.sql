-- Teste 3: Validações de campos obrigatórios e duplicados de nome
SET SERVEROUTPUT ON;

DECLARE
v_result NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('========================================');
    DBMS_OUTPUT.PUT_LINE('TESTE 5: CAMPOS OBRIGATÓRIOS E NOMES');
    DBMS_OUTPUT.PUT_LINE('========================================');
    DBMS_OUTPUT.PUT_LINE('');

    -- Teste 3a: Nome nulo
    DBMS_OUTPUT.PUT_LINE('Teste 5a: Nome nulo (NULL, 1520mm)');
    v_result := insert_gauge(NULL, 1520);
    DBMS_OUTPUT.PUT_LINE('Resultado: ' || v_result || ' (Esperado: 0)');
    IF v_result = 0 THEN
        DBMS_OUTPUT.PUT_LINE('✓ PASSOU');
ELSE
        DBMS_OUTPUT.PUT_LINE('✗ FALHOU');
END IF;
    DBMS_OUTPUT.PUT_LINE('');

    -- Teste 3b: Nome duplicado (case-insensitive)
    DBMS_OUTPUT.PUT_LINE('Teste 5b: Nome duplicado case-insensitive (BITOLA MÉTRICA, 1001mm)');
    v_result := insert_gauge('BITOLA MÉTRICA', 1001);
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