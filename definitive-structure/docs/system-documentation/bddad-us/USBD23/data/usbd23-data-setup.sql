-- 1. CLEANUP (Adapted from your USBD24 script)
-- Disable constraints temporarily to allow deletion
ALTER TABLE line DISABLE CONSTRAINT FKline57062;
ALTER TABLE line DISABLE CONSTRAINT FKline467100;
ALTER TABLE line DISABLE CONSTRAINT FKline868044;
/

-- Delete everything to ensure a clean slate
DELETE FROM train_composition;
DELETE FROM freights;
DELETE FROM segment;
DELETE FROM Gauge_line;
DELETE FROM line;
DELETE FROM Building;
DELETE FROM locomotive_operator;
DELETE FROM locomotive_Gauge;
DELETE FROM wagon_Gauge;
DELETE FROM train;
DELETE FROM wagon;
DELETE FROM wagon_model;
DELETE FROM locomotive;
DELETE FROM operator;
DELETE FROM owner;
DELETE FROM facility;
DELETE FROM building_type;
DELETE FROM gauge;
/

-- Re-enable constraints
ALTER TABLE line ENABLE CONSTRAINT FKline57062;
ALTER TABLE line ENABLE CONSTRAINT FKline467100;
ALTER TABLE line ENABLE CONSTRAINT FKline868044;
/

-- =================================================================
-- 2. INSERT TEST DATA FOR USBD23
-- =================================================================

-- 1. Insert Facilities (Start and End Points)
INSERT INTO facility (id_facility, name) VALUES (201, 'Lisboa Santa Apolonia');
INSERT INTO facility (id_facility, name) VALUES (202, 'Porto Campanha');

-- 2. Insert Owner (Infrastructure Manager)
INSERT INTO owner (id_owner, short_name, vatnumber) VALUES (201, 'InfraPortuguesa', '500000001');

-- 3. Insert Line (Connecting the two facilities)
INSERT INTO line (id_line, name_line, owner, start_id, start_name, end_id, end_name, gauge, ownerid_owner)
VALUES (201, 'Linha do Norte', 'InfraPortuguesa', 201, 'Lisboa Santa Apolonia', 202, 'Porto Campanha', 1668, 201);

-- 4. Insert Segments for the Line
-- Segment 1: Lisboa -> Entroncamento
INSERT INTO segment (id_segment, line_id, "order", eletrified, weight, length, number_tracks, siding_position, siding_lenght)
VALUES (201, 201, 1, HEXTORAW('01'), 5000, 100, '2', 0, 0);

-- Segment 2: Entroncamento -> Porto
INSERT INTO segment (id_segment, line_id, "order", eletrified, weight, length, number_tracks, siding_position, siding_lenght)
VALUES (202, 201, 2, HEXTORAW('01'), 5000, 200, '2', 50, 10);

-- 5. Insert Operator (Train Operator)
INSERT INTO operator (Id_operator, short_name_operator, vatnumber_operator)
VALUES (201, 'CP_Carga', '500000002');

-- 6. Insert Train (Associated with the specific start/end IDs of the line)
INSERT INTO train (id_train, operator, time, "date", start_id, end_id, freight, locomotive, path, operator_id)
VALUES (201, 1, TO_TIMESTAMP('08:00:00', 'HH24:MI:SS'), DATE '2025-12-01',
        201, -- Must match Line Start ID
        202, -- Must match Line End ID
        'Mercadorias', 'Loco_201', 'Lisboa, Santarem, Entroncamento, Coimbra, Aveiro, Porto', 201);

COMMIT;
/