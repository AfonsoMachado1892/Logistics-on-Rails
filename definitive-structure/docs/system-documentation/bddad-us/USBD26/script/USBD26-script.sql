CREATE OR REPLACE FUNCTION get_unused_wagons(
    p_start_date DATE,
    p_end_date DATE
) RETURN SYS_REFCURSOR IS
    result_cursor SYS_REFCURSOR;
BEGIN
    -- Abre o cursor para devolver os resultados
OPEN result_cursor FOR
SELECT
    w."number" AS "Wagon Number",
    wm.type AS "Wagon Type",
    wm.model_wagon AS "Model",
    op.short_name_operator AS "Operator"
FROM
    wagon w
        INNER JOIN
    wagon_model wm ON w.model_id = wm.id_model
        INNER JOIN
    operator op ON w.Operator_id = op.Id_operator
WHERE
    w."number" NOT IN (
        -- Sub-query: Encontra todos os vagões que FORAM usados no período
        SELECT DISTINCT
            tc.wagonnumber
        FROM
            train_composition tc
                INNER JOIN
            train t ON tc.trainid_train = t.id_train
        WHERE
            t."date" BETWEEN p_start_date AND p_end_date
    )
ORDER BY
    w."number" ASC;

RETURN result_cursor;
END;
/