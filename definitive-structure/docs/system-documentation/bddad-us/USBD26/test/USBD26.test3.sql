SET SERVEROUTPUT ON;
DECLARE
v_cursor SYS_REFCURSOR;
    v_number NUMBER;
    v_type   VARCHAR2(255);
    v_model  VARCHAR2(255);
    v_op     VARCHAR2(255);
BEGIN
    DBMS_OUTPUT.PUT_LINE('=== TESTE 3: Dia 06-Out-2025 (Exclui vagões do comboio 5437) ===');

    v_cursor := get_unused_wagons(TO_DATE('2025-10-06', 'YYYY-MM-DD'), TO_DATE('2025-10-06', 'YYYY-MM-DD'));

    LOOP
FETCH v_cursor INTO v_number, v_type, v_model, v_op;
        EXIT WHEN v_cursor%NOTFOUND;
        DBMS_OUTPUT.PUT_LINE('Vagão NÃO usado: ' || v_number || ' - ' || v_type);
END LOOP;

CLOSE v_cursor;
END;
/