SET SERVEROUTPUT ON;
SET VERIFY OFF;

DECLARE
v_cursor          SYS_REFCURSOR;
    -- Variables to match the columns returned by USBD23 function
    v_train_id        NUMBER;
    v_path            VARCHAR2(255);
    v_start_id        NUMBER;
    v_end_id          NUMBER;
    v_line_id         NUMBER;
    v_line_name       VARCHAR2(255);
    v_line_start      NUMBER;
    v_line_end        NUMBER;
    v_seg_id          NUMBER;
    v_seg_order       NUMBER;
    v_electrified     RAW(2000);
    v_seg_weight      NUMBER;
    v_seg_length      NUMBER;
    v_num_tracks      VARCHAR2(255); -- Note: Schema defines this as VARCHAR2
    v_siding_pos      NUMBER;
    v_siding_len      NUMBER;
    
    v_test_train_id   NUMBER := 201; -- The ID we inserted in setup
BEGIN
    DBMS_OUTPUT.PUT_LINE('Testing USBD23 for Train ID: ' || v_test_train_id);
    
    -- 1. Call the function
    v_cursor := get_train_route_segments(v_test_train_id);

    -- 2. Print Header
    DBMS_OUTPUT.PUT_LINE('--- Segments Found ---');
    DBMS_OUTPUT.PUT_LINE(
        RPAD('TrainID', 8) || ' | ' ||
        RPAD('Line Name', 20) || ' | ' ||
        RPAD('Seg ID', 8) || ' | ' ||
        RPAD('Order', 6) || ' | ' ||
        'Length'
    );
    DBMS_OUTPUT.PUT_LINE('-----------------------------------------------------------');

    -- 3. Loop through results
    LOOP
FETCH v_cursor INTO
            v_train_id, v_path, v_start_id, v_end_id, 
            v_line_id, v_line_name, v_line_start, v_line_end,
            v_seg_id, v_seg_order, v_electrified, v_seg_weight, 
            v_seg_length, v_num_tracks, v_siding_pos, v_siding_len;
            
        EXIT WHEN v_cursor%NOTFOUND;

        DBMS_OUTPUT.PUT_LINE(
            RPAD(v_train_id, 8) || ' | ' ||
            RPAD(v_line_name, 20) || ' | ' ||
            RPAD(v_seg_id, 8) || ' | ' ||
            RPAD(v_seg_order, 6) || ' | ' ||
            v_seg_length
        );
END LOOP;

CLOSE v_cursor;
END;
/