-- Teste 2: Validações de erro (duplicados, largura inválida e campos nulos)
SET SERVEROUTPUT ON;

DECLARE
v_result NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('========================================');
    DBMS_OUTPUT.PUT_LINE('TESTE 2: VALIDAÇÕES DE ERRO');
    DBMS_OUTPUT.PUT_LINE('========================================');
    DBMS_OUTPUT.PUT_LINE('');

    -- Teste 2a: Largura duplicada
    DBMS_OUTPUT.PUT_LINE('Teste 2a: Largura duplicada (1668mm já existe)');
    v_result := insert_gauge('Outra Ibérica', 1668);
    DBMS_OUTPUT.PUT_LINE('Resultado: ' || v_result || ' (Esperado: -1)');
    IF v_result = -1 THEN
        DBMS_OUTPUT.PUT_LINE('✓ PASSOU');
ELSE
        DBMS_OUTPUT.PUT_LINE('✗ FALHOU');
END IF;
    DBMS_OUTPUT.PUT_LINE('');

    -- Teste 2b: Largura negativa
    DBMS_OUTPUT.PUT_LINE('Teste 2b: Largura negativa (-100mm)');
    v_result := insert_gauge('Bitola Inválida', -100);
    DBMS_OUTPUT.PUT_LINE('Resultado: ' || v_result || ' (Esperado: -2)');
    IF v_result = -2 THEN
        DBMS_OUTPUT.PUT_LINE('✓ PASSOU');
ELSE
        DBMS_OUTPUT.PUT_LINE('✗ FALHOU');
END IF;
    DBMS_OUTPUT.PUT_LINE('');

    -- Teste 2c: Largura zero
    DBMS_OUTPUT.PUT_LINE('Teste 2c: Largura zero (0mm)');
    v_result := insert_gauge('Bitola Zero', 0);
    DBMS_OUTPUT.PUT_LINE('Resultado: ' || v_result || ' (Esperado: -2)');
    IF v_result = -2 THEN
        DBMS_OUTPUT.PUT_LINE('✓ PASSOU');
ELSE
        DBMS_OUTPUT.PUT_LINE('✗ FALHOU');
END IF;
    DBMS_OUTPUT.PUT_LINE('');

    -- Teste 2d: Largura nula
    DBMS_OUTPUT.PUT_LINE('Teste 2d: Largura nula (Bitola Teste, NULL)');
    v_result := insert_gauge('Bitola Teste', NULL);
    DBMS_OUTPUT.PUT_LINE('Resultado: ' || v_result || ' (Esperado: 0)');
    IF v_result = 0 THEN
        DBMS_OUTPUT.PUT_LINE('✓ PASSOU');
ELSE
        DBMS_OUTPUT.PUT_LINE('✗ FALHOU');
END IF;
    DBMS_OUTPUT.PUT_LINE('');

    DBMS_OUTPUT.PUT_LINE('========================================');
END;
/