SELECT
    t.id_train,
    t.path AS train_route,
    t.start_id,
    t.end_id,
    line.id_line,
    line.name_line,
    line.start_id AS line_start_id,
    line.end_id AS line_end_id, s.id_segment,
    s."order" AS segment_order,
    s.eletrified,
    s.weight AS segment_weight,
    s.length AS segment_length,
    s.number_tracks,
    s.siding_position,
    s.siding_lenght
FROM train t
         INNER JOIN line ON line.start_id=t.start_id and line.end_id=t.end_id
         INNER JOIN segment s ON line.id_line=s.line_id
WHERE t.id_train=5421