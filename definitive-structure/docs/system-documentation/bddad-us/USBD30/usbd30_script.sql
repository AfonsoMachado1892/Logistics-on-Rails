CREATE OR REPLACE FUNCTION get_facilities_silo_no_warehouse
RETURN SYS_REFCURSOR IS
    result_cursor SYS_REFCURSOR;
BEGIN
OPEN result_cursor FOR
SELECT
    f.id_facility AS "Facility ID",
    f.name AS "Facility Name"
FROM
    facility f
        INNER JOIN Building b ON f.id_facility = b.facility_id
        INNER JOIN building_type bt ON b.building_type_id = bt.id_building_type
WHERE
    bt.type = 'Grain Silo'

    MINUS

SELECT
    f.id_facility AS "Facility ID",
    f.name AS "Facility Name"
FROM
    facility f
        INNER JOIN Building b ON f.id_facility = b.facility_id
        INNER JOIN building_type bt ON b.building_type_id = bt.id_building_type
WHERE
    bt.type = 'Warehouse';

RETURN result_cursor;
END;
/