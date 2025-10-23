-- USBD15: As a planner, I want to know how many lines depart from a given endpoint.
--
-- A consulta solicita o nome de um endpoint (facility) e retorna
-- o número total de linhas que começam (departam) desse endpoint.

SELECT
    l.start_name AS endpoint_name,
    COUNT(l.id_line) AS number_of_lines_departing
FROM
    line l [cite: 3]
WHERE
    l.start_name = '&endpoint_name'
GROUP BY
    l.start_name;