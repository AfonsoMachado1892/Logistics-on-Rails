/* USBD30: As a planner, I want a list of all facilities that have grain silos, 
   but no warehouses.
*/

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