SELECT
    s.id_segment AS "Segment ID",
    s."order" AS "Segment Order",
    s.length AS "Length (m)",
    s.weight AS "Max Weight (kg/m)",
    s.eletrified AS "Electrified",
    s.number_tracks AS "Number of Tracks",
    l.id_line AS "Line ID",
    l.name_line AS "Line Name",
    l.start_name AS "Line Start",
    l.end_name AS "Line End",
    g.width AS "Gauge (mm)"
FROM
    segment s
        INNER JOIN line l ON s.line_id = l.id_line
        INNER JOIN Gauge_line gl ON l.id_line = gl.line_id
        INNER JOIN Gauge g ON gl.gauge_id = g.id_gauge
WHERE
    g.width = :p_gauge_width  -- Replace with desired gauge (e.g., 1668 or 1435)
ORDER BY
    l.id_line ASC,
    s."order" ASC;