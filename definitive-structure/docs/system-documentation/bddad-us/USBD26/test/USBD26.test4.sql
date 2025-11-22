SET SERVEROUTPUT ON;
DECLARE
v_cursor SYS_REFCURSOR;
    v_number NUMBER;
    v_type   VARCHAR2(255);
    v_model  VARCHAR2(255);
    v_op     VARCHAR2(255);
    v_count  NUMBER := 0;
BEGIN
    DBMS_OUTPUT.PUT_LINE('=== TESTE 4: Todo o mês de Outubro (01 a 31) ===');

    v_cursor := get_unused_wagons(TO_DATE('2025-10-01', 'YYYY-MM-DD'), TO_DATE('2025-10-31', 'YYYY-MM-DD'));

    LOOP
FETCH v_cursor INTO v_number, v_type, v_model, v_op;
        EXIT WHEN v_cursor%NOTFOUND;
        v_count := v_count + 1;
        DBMS_OUTPUT.PUT_LINE('Vagão parado o mês todo: ' || v_number);
END LOOP;

    DBMS_OUTPUT.PUT_LINE('Total de vagões sem qualquer serviço em Outubro: ' || v_count);
CLOSE v_cursor;
END;
/