-- Teste 4: Inserções bem-sucedidas de múltiplas bitolas
SET SERVEROUTPUT ON;

DECLARE
v_result NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('========================================');
    DBMS_OUTPUT.PUT_LINE('TESTE 4: INSERÇÕES BEM-SUCEDIDAS');
    DBMS_OUTPUT.PUT_LINE('========================================');
    DBMS_OUTPUT.PUT_LINE('');

    -- Teste 4a: Bitola Russa
    DBMS_OUTPUT.PUT_LINE('Teste 4a: Bitola Russa (1520mm)');
    v_result := insert_gauge('Bitola Russa', 1520);
    DBMS_OUTPUT.PUT_LINE('Resultado: ' || v_result || ' (Esperado: 1)');
    IF v_result = 1 THEN
        DBMS_OUTPUT.PUT_LINE('✓ PASSOU');
ELSE
        DBMS_OUTPUT.PUT_LINE('✗ FALHOU');
END IF;
    DBMS_OUTPUT.PUT_LINE('');

    -- Teste 4b: Bitola Estreita
    DBMS_OUTPUT.PUT_LINE('Teste 4b: Bitola Estreita (600mm)');
    v_result := insert_gauge('Bitola Estreita', 600);
    DBMS_OUTPUT.PUT_LINE('Resultado: ' || v_result || ' (Esperado: 1)');
    IF v_result = 1 THEN
        DBMS_OUTPUT.PUT_LINE('✓ PASSOU');
ELSE
        DBMS_OUTPUT.PUT_LINE('✗ FALHOU');
END IF;
    DBMS_OUTPUT.PUT_LINE('');

    -- Teste 4c: Bitola Indiana
    DBMS_OUTPUT.PUT_LINE('Teste 4c: Bitola Indiana (1676mm)');
    v_result := insert_gauge('Bitola Indiana', 1676);
    DBMS_OUTPUT.PUT_LINE('Resultado: ' || v_result || ' (Esperado: 1)');
    IF v_result = 1 THEN
        DBMS_OUTPUT.PUT_LINE('✓ PASSOU');
ELSE
        DBMS_OUTPUT.PUT_LINE('✗ FALHOU');
END IF;
    DBMS_OUTPUT.PUT_LINE('');

    DBMS_OUTPUT.PUT_LINE('========================================');
END;
/

-- Verificar as bitolas inseridas
SELECT * FROM Railway_Gauge WHERE gauge_value IN (1520, 600, 1676);

-- Listar todas as bitolas no sistema
PROMPT
PROMPT ========================================
PROMPT Todas as bitolas no sistema:
PROMPT ========================================
SELECT gauge_value AS "Largura (mm)",
       description AS "Descrição"
FROM Railway_Gauge
ORDER BY gauge_value;