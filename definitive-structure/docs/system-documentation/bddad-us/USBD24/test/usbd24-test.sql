SET SERVEROUTPUT ON;
SET VERIFY OFF;
SET ECHO OFF;

DECLARE
v_cursor SYS_REFCURSOR;
    v_id_train        NUMBER;
    v_id_start        NUMBER;
    v_name_start      VARCHAR2(255);
    v_id_end          NUMBER;
    v_name_end        VARCHAR2(255);
BEGIN
    -- Chamar a função
    v_cursor := get_all_train_endpoints();

    -- Imprimir o cabeçalho (Baseado nas colunas da função)
    DBMS_OUTPUT.PUT_LINE('--- Results ---');
    DBMS_OUTPUT.PUT_LINE('ID Comboio | ID Partida | Estação de Partida | ID Chegada | Estação de Chegada');
    DBMS_OUTPUT.PUT_LINE('-------------------------------------------------------------------------');

    -- Loop pelos resultados
    LOOP
FETCH v_cursor INTO v_id_train, v_id_start, v_name_start, v_id_end, v_name_end;
        EXIT WHEN v_cursor%NOTFOUND;

        -- Formatar e imprimir o output
        DBMS_OUTPUT.PUT_LINE(
            RPAD(v_id_train, 10) || ' | ' ||
            RPAD(v_id_start, 10) || ' | ' ||
            RPAD(v_name_start, 18) || ' | ' ||
            RPAD(v_id_end, 10) || ' | ' ||
            v_name_end
        );
END LOOP;
CLOSE v_cursor;
END;
/