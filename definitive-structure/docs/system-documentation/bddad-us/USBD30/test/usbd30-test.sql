SET SERVEROUTPUT ON;
DECLARE
    v_cursor SYS_REFCURSOR;
    v_id     NUMBER;
    v_name   VARCHAR2(255);
BEGIN
    v_cursor := get_facilities_silo_no_warehouse();

    DBMS_OUTPUT.PUT_LINE('--- Results ---');
    LOOP
        FETCH v_cursor INTO v_id, v_name;
        EXIT WHEN v_cursor%NOTFOUND;
        DBMS_OUTPUT.PUT_LINE(v_id || ' - ' || v_name);
    END LOOP;
    CLOSE v_cursor;
END;
/