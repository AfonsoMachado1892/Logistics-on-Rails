SET SERVEROUTPUT ON;
DECLARE
v_cursor SYS_REFCURSOR;
    v_number NUMBER;
    v_type   VARCHAR2(255);
    v_model  VARCHAR2(255);
    v_op     VARCHAR2(255);
    v_count  NUMBER := 0;
BEGIN
    DBMS_OUTPUT.PUT_LINE('=== TESTE 2: Dia 03-Out-2025 (Exclui vagões dos comboios 5421 e 5435) ===');

    v_cursor := get_unused_wagons(TO_DATE('2025-10-03', 'YYYY-MM-DD'), TO_DATE('2025-10-03', 'YYYY-MM-DD'));

    LOOP
FETCH v_cursor INTO v_number, v_type, v_model, v_op;
        EXIT WHEN v_cursor%NOTFOUND;
        v_count := v_count + 1;
        DBMS_OUTPUT.PUT_LINE('Vagão NÃO usado: ' || v_number);
END LOOP;

    DBMS_OUTPUT.PUT_LINE('Total de vagões parados neste dia: ' || v_count);
CLOSE v_cursor;
END;
/