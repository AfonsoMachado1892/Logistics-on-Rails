SELECT
    w.number AS "Wagon Number",
    w.model AS "Model",
    w.operator AS "Operator",
    w.service_year AS "Service Year",
    wm.id_model AS "Model ID",
    wm.model_wagon AS "Model Description",
    wm.maker AS "Maker",
    wm.type AS "Wagon Type",
    wm.bitola AS "Gauge (mm)",
    wm.lenght AS "Length (mm)",
    wm.width AS "Width (mm)",
    wm.height AS "Height (mm)",
    wm.max_speed AS "Max Speed (km/h)",
    wm.payload AS "Payload (t)",
    wm.volume AS "Volume (m³)",
    wm.number_bogies AS "Number of Bogies",
    wm.bogies AS "Bogie Type"
FROM
    wagon w
        INNER JOIN wagon_model wm ON w.model_id = wm.id_model
WHERE
    wm.bitola = :p_bitola  -- Replace with desired gauge (e.g., 1668 or 1435)
ORDER BY
    wm.type ASC,
    w.number ASC;