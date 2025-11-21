-- USBD17: As a planner, I want to know the average length of the segments in a given line, by track type.

SELECT
    l.id_line AS "Line ID",
    l.name_line AS "Line Name",
    s.number_tracks AS "Number of Tracks",
    ROUND(AVG(s.length), 2) AS "Average Segment Length (m)",
    COUNT(s.id_segment) AS "Total Segments"
FROM
    line l
        INNER JOIN segment s ON l.id_line = s.line_id
WHERE
    l.id_line = 1  -- Enter line ID (1, 2, ...)
GROUP BY
    l.id_line,
    l.name_line,
    s.number_tracks
ORDER BY
    s.number_tracks ASC;