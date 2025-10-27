SELECT
    f.id_facility AS "Facility ID",
    f.name AS "Facility Name",
    COUNT(l.id_line) AS "Number of Departing Lines"
FROM
    facility f
        LEFT JOIN line l ON f.id_facility = l.start_id
WHERE
    f.id_facility = :p_facility_id  -- Replace with desired facility ID
GROUP BY
    f.id_facility, f.name;
