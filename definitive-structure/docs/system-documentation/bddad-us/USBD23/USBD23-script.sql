SELECT
    t.id_train,
    t.path AS train_route,
    t.start_id,
    t.end_id,
    l.id_line,
    l.name_line,
    s.id_segment,
    s."order" AS segment_order,
    s.eletrified,
    s.weight AS segment_weight,
    s.length AS segment_length,
    s.number_tracks,
    s.siding_position,
    s.siding_lenght
FROM train t
         JOIN line l ON t.path LIKE '%' || l.name_line || '%'
         JOIN segment s ON s.line_id = l.id_line
WHERE t.id_train = :train_id  -- Replace with specific train ID
ORDER BY l.id_line, s."order";