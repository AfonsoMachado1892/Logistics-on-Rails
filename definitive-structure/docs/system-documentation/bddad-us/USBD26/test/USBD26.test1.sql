SET SERVEROUTPUT ON;
DECLARE
v_cursor SYS_REFCURSOR;
    v_number NUMBER;
    v_type   VARCHAR2(255);
    v_model  VARCHAR2(255);
    v_op     VARCHAR2(255);
BEGIN
    DBMS_OUTPUT.PUT_LINE('=== TESTE 1: Período Sem Comboios (Jan 2025) ===');

    -- Intervalo: 01-Jan-2025 a 31-Jan-2025
    v_cursor := get_unused_wagons(TO_DATE('2025-01-01', 'YYYY-MM-DD'), TO_DATE('2025-01-31', 'YYYY-MM-DD'));

    LOOP
FETCH v_cursor INTO v_number, v_type, v_model, v_op;
        EXIT WHEN v_cursor%NOTFOUND;
        -- Imprime apenas os primeiros para não encher o ecrã, ou comenta o contador para ver todos
        DBMS_OUTPUT.PUT_LINE('Vagão Disponível: ' || v_number || ' (' || v_model || ')');
END LOOP;

CLOSE v_cursor;
END;
/