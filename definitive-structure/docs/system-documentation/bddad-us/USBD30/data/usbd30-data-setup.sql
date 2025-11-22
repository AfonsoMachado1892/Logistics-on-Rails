-- Insert Building Types (Adding 'Maintenance' for variety)
INSERT INTO building_type (id_building_type, type) VALUES (1, 'Grain Silo');
INSERT INTO building_type (id_building_type, type) VALUES (2, 'Warehouse');
INSERT INTO building_type (id_building_type, type) VALUES (3, 'Passenger Terminal');
INSERT INTO building_type (id_building_type, type) VALUES (4, 'Maintenance Shop');


-- Assign Buildings to Facilities (Test Scenarios)

-- SHOULD APPEAR IN RESULTS (Positive Tests)

-- SCENARIO A: 'São Romão' (ID 1) -> Has 1 Grain Silo ONLY.
INSERT INTO Building (facility_id, name, building_type_id)
VALUES (1, 'Silo Alpha', 1);

-- SCENARIO E: 'Tamel' (ID 2) -> Has MULTIPLE Grain Silos (No Warehouses).
INSERT INTO Building (facility_id, name, building_type_id)
VALUES (2, 'Silo North', 1);
INSERT INTO Building (facility_id, name, building_type_id)
VALUES (2, 'Silo South', 1);

-- SCENARIO H: 'Barcelos' (ID 8) -> Has Silo + Irrelevant Building (Passenger Terminal).
INSERT INTO Building (facility_id, name, building_type_id)
VALUES (8, 'Cereal Silo', 1);
INSERT INTO Building (facility_id, name, building_type_id)
VALUES (8, 'Ticket Office', 3);




-- SHOULD NOT APPEAR (Negative Tests)


-- SCENARIO B: 'Leixões' (ID 50) -> Has Warehouse ONLY.
INSERT INTO Building (facility_id, name, building_type_id)
VALUES (50, 'Port Warehouse', 2);

-- SCENARIO C: 'Lousado' (ID 4) -> Has BOTH Silo and Warehouse.
INSERT INTO Building (facility_id, name, building_type_id)
VALUES (4, 'Silo Beta', 1);
INSERT INTO Building (facility_id, name, building_type_id)
VALUES (4, 'Central Warehouse', 2);

-- SCENARIO D: 'Porto Campanhã' (ID 5) -> Has Neither (Passenger Terminal only).
INSERT INTO Building (facility_id, name, building_type_id)
VALUES (5, 'Main Terminal', 3);

-- SCENARIO F: 'Senhora das Dores' (ID 3) -> Has MULTIPLE Warehouses (No Silos).
INSERT INTO Building (facility_id, name, building_type_id)
VALUES (3, 'Warehouse A', 2);
INSERT INTO Building (facility_id, name, building_type_id)
VALUES (3, 'Warehouse B', 2);

-- SCENARIO G: 'Leandro' (ID 6) -> Has Silo + Maintenance Shop + Warehouse.
INSERT INTO Building (facility_id, name, building_type_id)
VALUES (6, 'Silo X', 1);
INSERT INTO Building (facility_id, name, building_type_id)
VALUES (6, 'Repair Shop', 4);
INSERT INTO Building (facility_id, name, building_type_id)
VALUES (6, 'Logistics Warehouse', 2);

COMMIT;