SELECT
    w.number_wagon AS "Wagon Number",
    w.model AS "Wagon Model",
    w.operator AS "Wagon Operator",
    w.service_year AS "Service Year",
    wm.bitola AS "Gauge Width"
FROM
    wagon w
        JOIN
    wagon_model wm ON w.model_id = wm.id_model
WHERE
    wm.bitola = 1668 -- Replace with desired gauge (e.g., 1668 or 1435)