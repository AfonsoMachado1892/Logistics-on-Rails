CREATE OR REPLACE FUNCTION get_all_train_endpoints
RETURN SYS_REFCURSOR IS
    result_cursor SYS_REFCURSOR;
BEGIN
OPEN result_cursor FOR
SELECT
    t.id_train AS "ID Comboio",
    t.start_id AS "ID Partida",
    f_start.name AS "Estação de Partida",
    t.end_id AS "ID Chegada",
    f_end.name AS "Estação de Chegada"
FROM
    train t
        JOIN
    facility f_start ON t.start_id = f_start.id_facility
        JOIN
    facility f_end ON t.end_id = f_end.id_facility
ORDER BY
    t.id_train;

RETURN result_cursor;
END;
/