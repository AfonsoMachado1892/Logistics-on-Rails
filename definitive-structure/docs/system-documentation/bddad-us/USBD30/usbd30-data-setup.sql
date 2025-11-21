-- Insert Building Types
-- We need specific IDs for Silos and Warehouses to match the query logic later.
INSERT INTO building_type (id_building_type, type) VALUES (1, 'Grain Silo');
INSERT INTO building_type (id_building_type, type) VALUES (2, 'Warehouse');
INSERT INTO building_type (id_building_type, type) VALUES (3, 'Passenger Terminal');

-- 2. Assign Buildings to Facilities (Scenarios for Testing)

-- SCENARIO A: 'São Romão' (ID 1) -> Has Grain Silo ONLY.
-- EXPECTED RESULT: Should appear in the output.
INSERT INTO Building (facility_id, name, building_type_id) VALUES (1, 'Silo Alpha', 1);

-- SCENARIO B: 'Leixões' (ID 50) -> Has Warehouse ONLY.
-- EXPECTED RESULT: Should NOT appear.
INSERT INTO Building (facility_id, name, building_type_id) VALUES (50, 'Armazém Portuário', 2);

-- SCENARIO C: 'Lousado' (ID 4) -> Has BOTH Silo and Warehouse.
-- EXPECTED RESULT: Should NOT appear (fails "no warehouses" condition).
INSERT INTO Building (facility_id, name, building_type_id) VALUES (4, 'Silo Beta', 1);
INSERT INTO Building (facility_id, name, building_type_id) VALUES (4, 'Armazém Central', 2);

-- SCENARIO D: 'Porto Campanhã' (ID 5) -> Has Neither (Passenger Terminal only).
-- EXPECTED RESULT: Should NOT appear.
INSERT INTO Building (facility_id, name, building_type_id) VALUES (5, 'Terminal Passageiros', 3);