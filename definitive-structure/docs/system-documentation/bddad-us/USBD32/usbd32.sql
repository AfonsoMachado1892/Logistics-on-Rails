
/* ==========================================================================

3. INSERTS (DML) - Dados Normalizados

========================================================================== */



-- 3.1. Inserir Estações (Facility)

INSERT INTO Facility (ID, Name) VALUES (1, 'São Romão');

INSERT INTO Facility (ID, Name) VALUES (2, 'Tamel');

INSERT INTO Facility (ID, Name) VALUES (3, 'Senhora das Dores');

INSERT INTO Facility (ID, Name) VALUES (4, 'Lousado');

INSERT INTO Facility (ID, Name) VALUES (5, 'Porto Campanhã');

INSERT INTO Facility (ID, Name) VALUES (6, 'Leandro');

INSERT INTO Facility (ID, Name) VALUES (7, 'Porto São Bento');

INSERT INTO Facility (ID, Name) VALUES (8, 'Barcelos');

INSERT INTO Facility (ID, Name) VALUES (9, 'Vila Nova da Cerveira');

INSERT INTO Facility (ID, Name) VALUES (10, 'Midões');

INSERT INTO Facility (ID, Name) VALUES (11, 'Valença');

INSERT INTO Facility (ID, Name) VALUES (12, 'Darque');

INSERT INTO Facility (ID, Name) VALUES (13, 'Contumil');

INSERT INTO Facility (ID, Name) VALUES (14, 'Ermesinde');

INSERT INTO Facility (ID, Name) VALUES (15, 'São Frutuoso');

INSERT INTO Facility (ID, Name) VALUES (16, 'São Pedro da Torre');

INSERT INTO Facility (ID, Name) VALUES (17, 'Viana do Castelo');

INSERT INTO Facility (ID, Name) VALUES (18, 'Famalicão');

INSERT INTO Facility (ID, Name) VALUES (19, 'Barroselas');

INSERT INTO Facility (ID, Name) VALUES (20, 'Nine');

INSERT INTO Facility (ID, Name) VALUES (21, 'Caminha');

INSERT INTO Facility (ID, Name) VALUES (22, 'Carvalha');

INSERT INTO Facility (ID, Name) VALUES (23, 'Carreço');

INSERT INTO Facility (ID, Name) VALUES (30, 'Braga');

INSERT INTO Facility (ID, Name) VALUES (31, 'Manzagão');

INSERT INTO Facility (ID, Name) VALUES (32, 'Cerqueiral');

INSERT INTO Facility (ID, Name) VALUES (33, 'Gemieira');

INSERT INTO Facility (ID, Name) VALUES (35, 'Paredes de Coura');

INSERT INTO Facility (ID, Name) VALUES (43, 'São Gemil');

INSERT INTO Facility (ID, Name) VALUES (45, 'São Mamede de Infesta');

INSERT INTO Facility (ID, Name) VALUES (48, 'Leça do Balio');

INSERT INTO Facility (ID, Name) VALUES (50, 'Leixões');



-- 3.2. Inserir Proprietários e Operadores

INSERT INTO Line_Owner (vat_number, name, short_name) VALUES ('PT503933813', 'Infraestruturas de Portugal, SA', 'IP');



INSERT INTO Operator (vat_number, name, short_name) VALUES ('PT509017800', 'Medway - Operador Ferroviário de Mercadorias, S.A', 'Medway');

INSERT INTO Operator (vat_number, name, short_name) VALUES ('PT507832388', 'Captrain Portugal S.A.', 'Captrain');



-- 3.3. Inserir Linhas

-- Nota: Owner é sempre 'IP' (PT503933813) com base no dataset.

INSERT INTO Line (ID, Name, owner_vat, Start_Facility_ID, End_Facility_ID, Gauge) VALUES (1, 'Ramal São Bento - Campanhã', 'PT503933813', 7, 5, 1668);

INSERT INTO Line (ID, Name, owner_vat, Start_Facility_ID, End_Facility_ID, Gauge) VALUES (2, 'Ramal Camapanhã - Contumil', 'PT503933813', 5, 13, 1668);

INSERT INTO Line (ID, Name, owner_vat, Start_Facility_ID, End_Facility_ID, Gauge) VALUES (3, 'Ramal Contumil - Nine', 'PT503933813', 13, 20, 1668);

INSERT INTO Line (ID, Name, owner_vat, Start_Facility_ID, End_Facility_ID, Gauge) VALUES (4, 'Ramal Nine - Barcelos', 'PT503933813', 20, 8, 1668);

INSERT INTO Line (ID, Name, owner_vat, Start_Facility_ID, End_Facility_ID, Gauge) VALUES (5, 'Ramal Barcelos - Darque', 'PT503933813', 8, 12, 1668);

INSERT INTO Line (ID, Name, owner_vat, Start_Facility_ID, End_Facility_ID, Gauge) VALUES (6, 'Ramal Darque - Viana', 'PT503933813', 12, 17, 1668);

INSERT INTO Line (ID, Name, owner_vat, Start_Facility_ID, End_Facility_ID, Gauge) VALUES (7, 'Ramal Viana - Caminha', 'PT503933813', 17, 21, 1668);

INSERT INTO Line (ID, Name, owner_vat, Start_Facility_ID, End_Facility_ID, Gauge) VALUES (8, 'Ramal Caminha - Torre', 'PT503933813', 21, 16, 1668);

INSERT INTO Line (ID, Name, owner_vat, Start_Facility_ID, End_Facility_ID, Gauge) VALUES (9, 'Ramal Torre - Valença', 'PT503933813', 16, 11, 1668);

INSERT INTO Line (ID, Name, owner_vat, Start_Facility_ID, End_Facility_ID, Gauge) VALUES (21, 'Ramal Contumil - São Gemil', 'PT503933813', 13, 43, 1668);

INSERT INTO Line (ID, Name, owner_vat, Start_Facility_ID, End_Facility_ID, Gauge) VALUES (22, 'Ramal São Gemil - São Mamede de Infesta', 'PT503933813', 43, 45, 1668);

INSERT INTO Line (ID, Name, owner_vat, Start_Facility_ID, End_Facility_ID, Gauge) VALUES (23, 'Ramal São Mamede de Infesta - Leça do Balio', 'PT503933813', 45, 48, 1668);

INSERT INTO Line (ID, Name, owner_vat, Start_Facility_ID, End_Facility_ID, Gauge) VALUES (24, 'Ramal Leça do Balio - Leixões', 'PT503933813', 48, 50, 1668);

INSERT INTO Line (ID, Name, owner_vat, Start_Facility_ID, End_Facility_ID, Gauge) VALUES (30, 'Ramal Braga', 'PT503933813', 31, 30, 1668);

INSERT INTO Line (ID, Name, owner_vat, Start_Facility_ID, End_Facility_ID, Gauge) VALUES (31, 'Ramal Nine - Manzagão', 'PT503933813', 20, 31, 1668);

INSERT INTO Line (ID, Name, owner_vat, Start_Facility_ID, End_Facility_ID, Gauge) VALUES (32, 'Ramal Manzagão - Cerqueiral', 'PT503933813', 31, 32, 1668);

INSERT INTO Line (ID, Name, owner_vat, Start_Facility_ID, End_Facility_ID, Gauge) VALUES (35, 'Ramal Cerqueiral - Gemieira', 'PT503933813', 32, 33, 1668);

INSERT INTO Line (ID, Name, owner_vat, Start_Facility_ID, End_Facility_ID, Gauge) VALUES (36, 'Ramal Gemieira - Paredes de Coura', 'PT503933813', 33, 35, 1668);

INSERT INTO Line (ID, Name, owner_vat, Start_Facility_ID, End_Facility_ID, Gauge) VALUES (37, 'Ramal Paredes de Coura - Valença', 'PT503933813', 35, 11, 1668);



-- 3.4. Inserir Segmentos

INSERT INTO Segment (ID, Line_ID, sequence_order, Electrified, Max_Weight_kg_m, Length_m, Number_Tracks, Siding_Position, Siding_Length) VALUES (1, 1, 1, 'Yes', 8000, 2618, 4, NULL, NULL);

INSERT INTO Segment (ID, Line_ID, sequence_order, Electrified, Max_Weight_kg_m, Length_m, Number_Tracks, Siding_Position, Siding_Length) VALUES (3, 2, 1, 'Yes', 8000, 2443, 4, NULL, NULL);

INSERT INTO Segment (ID, Line_ID, sequence_order, Electrified, Max_Weight_kg_m, Length_m, Number_Tracks, Siding_Position, Siding_Length) VALUES (10, 3, 1, 'Yes', 8000, 26560, 2, NULL, NULL);

INSERT INTO Segment (ID, Line_ID, sequence_order, Electrified, Max_Weight_kg_m, Length_m, Number_Tracks, Siding_Position, Siding_Length) VALUES (11, 3, 2, 'Yes', 8000, 10000, 2, NULL, NULL);

INSERT INTO Segment (ID, Line_ID, sequence_order, Electrified, Max_Weight_kg_m, Length_m, Number_Tracks, Siding_Position, Siding_Length) VALUES (15, 4, 1, 'Yes', 8000, 5286, 2, NULL, NULL);

INSERT INTO Segment (ID, Line_ID, sequence_order, Electrified, Max_Weight_kg_m, Length_m, Number_Tracks, Siding_Position, Siding_Length) VALUES (16, 4, 2, 'Yes', 8000, 6000, 2, NULL, NULL);

INSERT INTO Segment (ID, Line_ID, sequence_order, Electrified, Max_Weight_kg_m, Length_m, Number_Tracks, Siding_Position, Siding_Length) VALUES (14, 5, 1, 'Yes', 8000, 10387, 2, NULL, NULL);

INSERT INTO Segment (ID, Line_ID, sequence_order, Electrified, Max_Weight_kg_m, Length_m, Number_Tracks, Siding_Position, Siding_Length) VALUES (12, 5, 2, 'Yes', 8000, 12000, 2, NULL, NULL);

INSERT INTO Segment (ID, Line_ID, sequence_order, Electrified, Max_Weight_kg_m, Length_m, Number_Tracks, Siding_Position, Siding_Length) VALUES (13, 5, 3, 'Yes', 8000, 3100, 2, NULL, NULL);

INSERT INTO Segment (ID, Line_ID, sequence_order, Electrified, Max_Weight_kg_m, Length_m, Number_Tracks, Siding_Position, Siding_Length) VALUES (20, 6, 1, 'Yes', 6400, 4890, 2, NULL, NULL);

INSERT INTO Segment (ID, Line_ID, sequence_order, Electrified, Max_Weight_kg_m, Length_m, Number_Tracks, Siding_Position, Siding_Length) VALUES (18, 7, 1, 'Yes', 8000, 6000, 1, NULL, NULL);

INSERT INTO Segment (ID, Line_ID, sequence_order, Electrified, Max_Weight_kg_m, Length_m, Number_Tracks, Siding_Position, Siding_Length) VALUES (21, 7, 2, 'Yes', 8000, 5000, 1, 2000, 864);

INSERT INTO Segment (ID, Line_ID, sequence_order, Electrified, Max_Weight_kg_m, Length_m, Number_Tracks, Siding_Position, Siding_Length) VALUES (22, 7, 3, 'Yes', 8000, 12000, 1, NULL, NULL);

INSERT INTO Segment (ID, Line_ID, sequence_order, Electrified, Max_Weight_kg_m, Length_m, Number_Tracks, Siding_Position, Siding_Length) VALUES (25, 8, 1, 'Yes', 8000, 20829, 1, 11000, 266);

INSERT INTO Segment (ID, Line_ID, sequence_order, Electrified, Max_Weight_kg_m, Length_m, Number_Tracks, Siding_Position, Siding_Length) VALUES (26, 9, 1, 'Yes', 8000, 4264, 1, NULL, NULL);

INSERT INTO Segment (ID, Line_ID, sequence_order, Electrified, Max_Weight_kg_m, Length_m, Number_Tracks, Siding_Position, Siding_Length) VALUES (30, 21, 1, 'Yes', 8000, 3883, 2, NULL, NULL);

INSERT INTO Segment (ID, Line_ID, sequence_order, Electrified, Max_Weight_kg_m, Length_m, Number_Tracks, Siding_Position, Siding_Length) VALUES (31, 22, 1, 'Yes', 8400, 1174, 2, NULL, NULL);

INSERT INTO Segment (ID, Line_ID, sequence_order, Electrified, Max_Weight_kg_m, Length_m, Number_Tracks, Siding_Position, Siding_Length) VALUES (32, 22, 2, 'Yes', 8000, 2534, 2, NULL, NULL);

INSERT INTO Segment (ID, Line_ID, sequence_order, Electrified, Max_Weight_kg_m, Length_m, Number_Tracks, Siding_Position, Siding_Length) VALUES (33, 23, 1, 'Yes', 8000, 1566, 2, NULL, NULL);

INSERT INTO Segment (ID, Line_ID, sequence_order, Electrified, Max_Weight_kg_m, Length_m, Number_Tracks, Siding_Position, Siding_Length) VALUES (34, 23, 2, 'Yes', 8000, 1453, 2, NULL, NULL);

INSERT INTO Segment (ID, Line_ID, sequence_order, Electrified, Max_Weight_kg_m, Length_m, Number_Tracks, Siding_Position, Siding_Length) VALUES (35, 24, 1, 'Yes', 8100, 3597, 2, NULL, NULL);

INSERT INTO Segment (ID, Line_ID, sequence_order, Electrified, Max_Weight_kg_m, Length_m, Number_Tracks, Siding_Position, Siding_Length) VALUES (36, 24, 2, 'Yes', 8000, 4334, 2, NULL, NULL);

INSERT INTO Segment (ID, Line_ID, sequence_order, Electrified, Max_Weight_kg_m, Length_m, Number_Tracks, Siding_Position, Siding_Length) VALUES (50, 30, 1, 'Yes', 8000, 3555, 2, NULL, NULL);

INSERT INTO Segment (ID, Line_ID, sequence_order, Electrified, Max_Weight_kg_m, Length_m, Number_Tracks, Siding_Position, Siding_Length) VALUES (55, 31, 1, 'Yes', 8000, 3470, 2, NULL, NULL);

INSERT INTO Segment (ID, Line_ID, sequence_order, Electrified, Max_Weight_kg_m, Length_m, Number_Tracks, Siding_Position, Siding_Length) VALUES (54, 31, 2, 'Yes', 8000, 3350, 2, NULL, NULL);

INSERT INTO Segment (ID, Line_ID, sequence_order, Electrified, Max_Weight_kg_m, Length_m, Number_Tracks, Siding_Position, Siding_Length) VALUES (53, 31, 3, 'Yes', 8000, 1720, 2, NULL, NULL);

INSERT INTO Segment (ID, Line_ID, sequence_order, Electrified, Max_Weight_kg_m, Length_m, Number_Tracks, Siding_Position, Siding_Length) VALUES (52, 31, 4, 'Yes', 8000, 1760, 2, NULL, NULL);

INSERT INTO Segment (ID, Line_ID, sequence_order, Electrified, Max_Weight_kg_m, Length_m, Number_Tracks, Siding_Position, Siding_Length) VALUES (51, 31, 5, 'Yes', 8000, 1222, 2, NULL, NULL);

INSERT INTO Segment (ID, Line_ID, sequence_order, Electrified, Max_Weight_kg_m, Length_m, Number_Tracks, Siding_Position, Siding_Length) VALUES (58, 32, 1, 'Yes', 8000, 8050, 2, NULL, NULL);

INSERT INTO Segment (ID, Line_ID, sequence_order, Electrified, Max_Weight_kg_m, Length_m, Number_Tracks, Siding_Position, Siding_Length) VALUES (59, 35, 1, 'Yes', 8000, 22320, 2, NULL, NULL);

INSERT INTO Segment (ID, Line_ID, sequence_order, Electrified, Max_Weight_kg_m, Length_m, Number_Tracks, Siding_Position, Siding_Length) VALUES (60, 36, 1, 'Yes', 8000, 16310, 2, NULL, NULL);

INSERT INTO Segment (ID, Line_ID, sequence_order, Electrified, Max_Weight_kg_m, Length_m, Number_Tracks, Siding_Position, Siding_Length) VALUES (61, 37, 1, 'Yes', 8000, 15200, 2, NULL, NULL);



-- 3.5. Inserir Locomotivas

-- Medway (PT509017800), Captrain (PT507832388)

INSERT INTO Locomotive VALUES ('5621', 'Inês', 'Siemens', 'Eurosprinter', TO_DATE('1995-01-01','YYYY-MM-DD'), 2, 'Bo-Bo', 5600, 19.2, 3, 4.375, 87, 220, 70, 300, 'Electric', '25 KV', '50 Hz', 'PT509017800', 1668, NULL);

INSERT INTO Locomotive VALUES ('5623', 'Paz', 'Siemens', 'Eurosprinter', TO_DATE('1995-04-01','YYYY-MM-DD'), 2, 'Bo-Bo', 5600, 19.2, 3, 4.375, 87, 220, 70, 300, 'Electric', '25 KV', '50 Hz', 'PT509017800', 1668, NULL);

INSERT INTO Locomotive VALUES ('5630', 'Helena', 'Siemens', 'Eurosprinter', TO_DATE('1996-01-02','YYYY-MM-DD'), 2, 'Bo-Bo', 5600, 19.2, 3, 4.375, 87, 220, 70, 300, 'Electric', '25 KV', '50 Hz', 'PT509017800', 1668, NULL);

INSERT INTO Locomotive VALUES ('1903', 'Eva', 'Sorefame - Alsthom', 'CP 1900', TO_DATE('1981-04-07','YYYY-MM-DD'), 2, 'Co-Co', 1623, 19.084, 3.062, 4.31, 117, 100, 42.5, 396, 'Diesel', NULL, NULL, 'PT509017800', 1668, 4882);

INSERT INTO Locomotive VALUES ('5034', 'Adriana', 'Stadler', 'E4000', TO_DATE('2017-02-15','YYYY-MM-DD'), 3, 'Co-Co', 3178, 23.02, 3, 4.264, 124, 120, 100, 400, 'Diesel', NULL, NULL, 'PT509017800', 1668, 6700);

INSERT INTO Locomotive VALUES ('5036', 'Marina', 'Stadler', 'E4000', TO_DATE('2017-02-01','YYYY-MM-DD'), 3, 'Co-Co', 3178, 23.02, 3, 4.264, 124, 120, 100, 400, 'Diesel', NULL, NULL, 'PT509017800', 1668, 6700);

-- Nota: O dataset tem uma entrada duplicada para 5036 com Gauge 1435. Como a PK é o número, isto falharia. Assumirei que é a mesma loco adaptada ou erro, mas inseri apenas a primeira (1668).

INSERT INTO Locomotive VALUES ('335.001', NULL, 'Stadler', 'E4000', TO_DATE('2019-05-07','YYYY-MM-DD'), 3, 'Co-Co', 3178, 23.02, 3, 4.264, 124, 120, 100, 400, 'Diesel', NULL, NULL, 'PT507832388', 1668, 6700);

INSERT INTO Locomotive VALUES ('335.003', NULL, 'Stadler', 'E4000', TO_DATE('2019-06-03','YYYY-MM-DD'), 3, 'Co-Co', 3178, 23.02, 3, 4.264, 124, 120, 100, 400, 'Diesel', NULL, NULL, 'PT507832388', 1668, 6700);



-- 3.6. Inserir Modelos de Vagões

INSERT INTO Wagon_Model VALUES (1245, 'Tadgs 32 94 082 3', 'Metalsines', 2, 'Duplo', 17240, 3072, 4270, 24, 120, 56, 75, 'Cereal wagon', 1668);

INSERT INTO Wagon_Model VALUES (1278, 'Tdgs 41 94 074 1', 'Equimetal', 2, 'Duplo', 9640, 3120, 4165.5, 13.8, 100, 26.2, 38, 'Cereal wagon', 1668);

INSERT INTO Wagon_Model VALUES (1325, 'Gabs 81 94 181 1', 'Sepsa Cometna', 2, 'Duplo', 21700, 3180, 4170, 29.8, 100, 50.2, 110, 'Covered wagon with sliding door', 1668);

INSERT INTO Wagon_Model VALUES (1104, 'Regmms 32 94 356 3', 'Metalsines', 2, 'Duplo', 14040, 3104, 2535, 21.2, 120, 60.6, 76.3, 'Container wagon (max 40 HC)', 1668);

INSERT INTO Wagon_Model VALUES (985, 'Lgs 22 94 441 6', 'Metalsines', 2, 'Simples', 13860, 2850, 1060, 11.9, 120, 28.1, 76.3, 'Container wagon (max 40 HC)', 1668);

INSERT INTO Wagon_Model VALUES (987, 'Sgnss 12 94 455 2', 'Emef', 2, 'Duplo', 18116, 2950, 1030, 21.6, 120, 68.4, 76.3, 'Container wagon (max 40 HC)', 1668);

INSERT INTO Wagon_Model VALUES (988, 'Sgnss 12 94 455 2', 'Emef', 2, 'Duplo', 18116, 2950, 1030, 21.6, 120, 68.4, 76.3, 'Container wagon (max 40 HC)', 1435);

INSERT INTO Wagon_Model VALUES (1525, 'Zaes 81 94 788', 'Equimetal', 2, 'Duplo', 13800, 2950, 4226, 22.9, 120, 57.1, 64.6, 'Biodiesel wagaon', 1668);

INSERT INTO Wagon_Model VALUES (1523, 'Zaes 81 94 788', 'Equimetal', 2, 'Duplo', 13800, 2950, 4226, 22.9, 120, 57.1, 64.6, 'Biodiesel wagaon', 1435);

INSERT INTO Wagon_Model VALUES (1212, 'Kbs 41 94 333', 'Simmering', 2, 'Simples', 14020, 2842, 3300, 14.2, 100, 25.8, 62.7, 'Wood wagon', 1668);



-- 3.7. Inserir Vagões (Amostra baseada no ficheiro)

-- Operador Medway

INSERT INTO Wagon (wagon_number, model_id, operator_vat, service_start_date) VALUES ('356 3 077', 1104, 'PT509017800', TO_DATE('1987-05-02','YYYY-MM-DD'));

INSERT INTO Wagon (wagon_number, model_id, operator_vat, service_start_date) VALUES ('356 3 078', 1104, 'PT509017800', TO_DATE('1987-05-02','YYYY-MM-DD'));

INSERT INTO Wagon (wagon_number, model_id, operator_vat, service_start_date) VALUES ('356 3 079', 1104, 'PT509017800', TO_DATE('1987-05-02','YYYY-MM-DD'));

INSERT INTO Wagon (wagon_number, model_id, operator_vat, service_start_date) VALUES ('356 3 080', 1104, 'PT509017800', TO_DATE('1987-05-02','YYYY-MM-DD'));

INSERT INTO Wagon (wagon_number, model_id, operator_vat, service_start_date) VALUES ('356 3 089', 1104, 'PT509017800', TO_DATE('1987-05-02','YYYY-MM-DD'));

INSERT INTO Wagon (wagon_number, model_id, operator_vat, service_start_date) VALUES ('356 3 090', 1104, 'PT509017800', TO_DATE('1987-05-02','YYYY-MM-DD'));

INSERT INTO Wagon (wagon_number, model_id, operator_vat, service_start_date) VALUES ('356 3 091', 1104, 'PT509017800', TO_DATE('1987-05-02','YYYY-MM-DD'));

INSERT INTO Wagon (wagon_number, model_id, operator_vat, service_start_date) VALUES ('356 3 092', 1104, 'PT509017800', TO_DATE('1987-05-02','YYYY-MM-DD'));

INSERT INTO Wagon (wagon_number, model_id, operator_vat, service_start_date) VALUES ('181 1 011', 1325, 'PT509017800', TO_DATE('1977-02-03','YYYY-MM-DD'));

INSERT INTO Wagon (wagon_number, model_id, operator_vat, service_start_date) VALUES ('181 1 012', 1325, 'PT509017800', TO_DATE('1977-02-03','YYYY-MM-DD'));

INSERT INTO Wagon (wagon_number, model_id, operator_vat, service_start_date) VALUES ('181 1 013', 1325, 'PT509017800', TO_DATE('1977-02-03','YYYY-MM-DD'));

INSERT INTO Wagon (wagon_number, model_id, operator_vat, service_start_date) VALUES ('333 0 001', 1212, 'PT509017800', TO_DATE('2005-09-01','YYYY-MM-DD'));

INSERT INTO Wagon (wagon_number, model_id, operator_vat, service_start_date) VALUES ('333 0 002', 1212, 'PT509017800', TO_DATE('2005-09-01','YYYY-MM-DD'));

INSERT INTO Wagon (wagon_number, model_id, operator_vat, service_start_date) VALUES ('333 0 003', 1212, 'PT509017800', TO_DATE('2005-09-01','YYYY-MM-DD'));

INSERT INTO Wagon (wagon_number, model_id, operator_vat, service_start_date) VALUES ('333 0 004', 1212, 'PT509017800', TO_DATE('2005-09-01','YYYY-MM-DD'));

INSERT INTO Wagon (wagon_number, model_id, operator_vat, service_start_date) VALUES ('333 0 005', 1212, 'PT509017800', TO_DATE('2005-09-01','YYYY-MM-DD'));

INSERT INTO Wagon (wagon_number, model_id, operator_vat, service_start_date) VALUES ('333 0 006', 1212, 'PT509017800', TO_DATE('2005-09-01','YYYY-MM-DD'));

INSERT INTO Wagon (wagon_number, model_id, operator_vat, service_start_date) VALUES ('333 0 007', 1212, 'PT509017800', TO_DATE('2005-09-01','YYYY-MM-DD'));



-- 3.8. Inserir Freights e Associação com Vagões

-- Freight 2001

INSERT INTO Freight (ID, freight_date, origin_facility_id, dest_facility_id) VALUES (2001, TO_DATE('2025-10-03','YYYY-MM-DD'), 50, 12);

INSERT INTO Freight_Wagon VALUES (2001, '333 0 001');

INSERT INTO Freight_Wagon VALUES (2001, '333 0 002');

INSERT INTO Freight_Wagon VALUES (2001, '333 0 004');

INSERT INTO Freight_Wagon VALUES (2001, '333 0 005');

INSERT INTO Freight_Wagon VALUES (2001, '333 0 006');



-- Freight 2002

INSERT INTO Freight (ID, freight_date, origin_facility_id, dest_facility_id) VALUES (2002, TO_DATE('2025-10-03','YYYY-MM-DD'), 13, 11);

INSERT INTO Freight_Wagon VALUES (2002, '356 3 089');



-- Freight 2003

INSERT INTO Freight (ID, freight_date, origin_facility_id, dest_facility_id) VALUES (2003, TO_DATE('2025-10-03','YYYY-MM-DD'), 50, 11);

INSERT INTO Freight_Wagon VALUES (2003, '181 1 011');

INSERT INTO Freight_Wagon VALUES (2003, '181 1 012');



-- Freight 2004

INSERT INTO Freight (ID, freight_date, origin_facility_id, dest_facility_id) VALUES (2004, TO_DATE('2025-10-03','YYYY-MM-DD'), 50, 21);

INSERT INTO Freight_Wagon VALUES (2004, '181 1 013');



-- Freight 2005

INSERT INTO Freight (ID, freight_date, origin_facility_id, dest_facility_id) VALUES (2005, TO_DATE('2025-10-03','YYYY-MM-DD'), 20, 11);

INSERT INTO Freight_Wagon VALUES (2005, '356 3 077');

INSERT INTO Freight_Wagon VALUES (2005, '356 3 078');

INSERT INTO Freight_Wagon VALUES (2005, '356 3 079');

INSERT INTO Freight_Wagon VALUES (2005, '356 3 080');



-- Freight 2006

INSERT INTO Freight (ID, freight_date, origin_facility_id, dest_facility_id) VALUES (2006, TO_DATE('2025-10-03','YYYY-MM-DD'), 12, 50);

INSERT INTO Freight_Wagon VALUES (2006, '333 0 003');

INSERT INTO Freight_Wagon VALUES (2006, '333 0 007');



-- Freight 2007

INSERT INTO Freight (ID, freight_date, origin_facility_id, dest_facility_id) VALUES (2007, TO_DATE('2025-10-03','YYYY-MM-DD'), 50, 5);

INSERT INTO Freight_Wagon VALUES (2007, '356 3 090');

INSERT INTO Freight_Wagon VALUES (2007, '356 3 091');

INSERT INTO Freight_Wagon VALUES (2007, '356 3 092');



-- Freight 2050

INSERT INTO Freight (ID, freight_date, origin_facility_id, dest_facility_id) VALUES (2050, TO_DATE('2025-10-06','YYYY-MM-DD'), 12, 50);

INSERT INTO Freight_Wagon VALUES (2050, '333 0 001');

INSERT INTO Freight_Wagon VALUES (2050, '333 0 002');

INSERT INTO Freight_Wagon VALUES (2050, '333 0 004');

INSERT INTO Freight_Wagon VALUES (2050, '333 0 005');

INSERT INTO Freight_Wagon VALUES (2050, '333 0 006');



-- Freight 2051

INSERT INTO Freight (ID, freight_date, origin_facility_id, dest_facility_id) VALUES (2051, TO_DATE('2025-10-06','YYYY-MM-DD'), 11, 50);

INSERT INTO Freight_Wagon VALUES (2051, '181 1 011');

INSERT INTO Freight_Wagon VALUES (2051, '181 1 012');

INSERT INTO Freight_Wagon VALUES (2051, '356 3 077');

INSERT INTO Freight_Wagon VALUES (2051, '356 3 078');

INSERT INTO Freight_Wagon VALUES (2051, '356 3 079');

INSERT INTO Freight_Wagon VALUES (2051, '356 3 080');



-- 3.9. Inserir Comboios (Trains) e Associações

-- Train 5421

INSERT INTO Train (ID, operator_vat, departure_date, departure_time, start_facility_id, end_facility_id)

VALUES (5421, 'PT509017800', TO_DATE('2025-10-03','YYYY-MM-DD'), '09:45:00', 50, 11);



-- Train 5421 Freights

INSERT INTO Train_Freight VALUES (5421, 2001);

INSERT INTO Train_Freight VALUES (5421, 2002);

INSERT INTO Train_Freight VALUES (5421, 2003);

INSERT INTO Train_Freight VALUES (5421, 2004);

INSERT INTO Train_Freight VALUES (5421, 2005);



-- Train 5421 Locomotives

INSERT INTO Train_Locomotive VALUES (5421, '5621');

INSERT INTO Train_Locomotive VALUES (5421, '5623');



-- Train 5421 Path (Sequence)

INSERT INTO Train_Path (train_id, sequence_order, facility_id) VALUES (5421, 1, 50);

INSERT INTO Train_Path (train_id, sequence_order, facility_id) VALUES (5421, 2, 48);

INSERT INTO Train_Path (train_id, sequence_order, facility_id) VALUES (5421, 3, 45);

INSERT INTO Train_Path (train_id, sequence_order, facility_id) VALUES (5421, 4, 43);

INSERT INTO Train_Path (train_id, sequence_order, facility_id) VALUES (5421, 5, 13);

INSERT INTO Train_Path (train_id, sequence_order, facility_id) VALUES (5421, 6, 20);

INSERT INTO Train_Path (train_id, sequence_order, facility_id) VALUES (5421, 7, 8);

INSERT INTO Train_Path (train_id, sequence_order, facility_id) VALUES (5421, 8, 12);

INSERT INTO Train_Path (train_id, sequence_order, facility_id) VALUES (5421, 9, 17);

INSERT INTO Train_Path (train_id, sequence_order, facility_id) VALUES (5421, 10, 21);

INSERT INTO Train_Path (train_id, sequence_order, facility_id) VALUES (5421, 11, 16);

INSERT INTO Train_Path (train_id, sequence_order, facility_id) VALUES (5421, 12, 11);





-- Train 5435

INSERT INTO Train (ID, operator_vat, departure_date, departure_time, start_facility_id, end_facility_id)

VALUES (5435, 'PT509017800', TO_DATE('2025-10-03','YYYY-MM-DD'), '18:00:00', 11, 50);



-- Train 5435 Freights

INSERT INTO Train_Freight VALUES (5435, 2006);



-- Train 5435 Locomotives

INSERT INTO Train_Locomotive VALUES (5435, '5623');



-- Train 5435 Path

INSERT INTO Train_Path (train_id, sequence_order, facility_id) VALUES (5435, 1, 11);

INSERT INTO Train_Path (train_id, sequence_order, facility_id) VALUES (5435, 2, 16);

INSERT INTO Train_Path (train_id, sequence_order, facility_id) VALUES (5435, 3, 21);

INSERT INTO Train_Path (train_id, sequence_order, facility_id) VALUES (5435, 4, 17);

INSERT INTO Train_Path (train_id, sequence_order, facility_id) VALUES (5435, 5, 12);

INSERT INTO Train_Path (train_id, sequence_order, facility_id) VALUES (5435, 6, 8);

INSERT INTO Train_Path (train_id, sequence_order, facility_id) VALUES (5435, 7, 20);

INSERT INTO Train_Path (train_id, sequence_order, facility_id) VALUES (5435, 8, 13);

INSERT INTO Train_Path (train_id, sequence_order, facility_id) VALUES (5435, 9, 43);

INSERT INTO Train_Path (train_id, sequence_order, facility_id) VALUES (5435, 10, 45);

INSERT INTO Train_Path (train_id, sequence_order, facility_id) VALUES (5435, 11, 48);

INSERT INTO Train_Path (train_id, sequence_order, facility_id) VALUES (5435, 12, 50);





-- Train 5437

INSERT INTO Train (ID, operator_vat, departure_date, departure_time, start_facility_id, end_facility_id)

VALUES (5437, 'PT509017800', TO_DATE('2025-10-06','YYYY-MM-DD'), '10:00:00', 11, 50);



-- Train 5437 Freights

INSERT INTO Train_Freight VALUES (5437, 2050);

INSERT INTO Train_Freight VALUES (5437, 2051);



-- Train 5437 Locomotives

INSERT INTO Train_Locomotive VALUES (5437, '5621');



-- Train 5437 Path

INSERT INTO Train_Path (train_id, sequence_order, facility_id) VALUES (5437, 1, 11);

INSERT INTO Train_Path (train_id, sequence_order, facility_id) VALUES (5437, 2, 16);

INSERT INTO Train_Path (train_id, sequence_order, facility_id) VALUES (5437, 3, 21);

INSERT INTO Train_Path (train_id, sequence_order, facility_id) VALUES (5437, 4, 17);

INSERT INTO Train_Path (train_id, sequence_order, facility_id) VALUES (5437, 5, 12);

INSERT INTO Train_Path (train_id, sequence_order, facility_id) VALUES (5437, 6, 8);

INSERT INTO Train_Path (train_id, sequence_order, facility_id) VALUES (5437, 7, 20);

INSERT INTO Train_Path (train_id, sequence_order, facility_id) VALUES (5437, 8, 13);

INSERT INTO Train_Path (train_id, sequence_order, facility_id) VALUES (5437, 9, 43);

INSERT INTO Train_Path (train_id, sequence_order, facility_id) VALUES (5437, 10, 45);

INSERT INTO Train_Path (train_id, sequence_order, facility_id) VALUES (5437, 11, 48);

INSERT INTO Train_Path (train_id, sequence_order, facility_id) VALUES (5437, 12, 50);


-- 3.10. Inicializar localização dos vagões (Correção para USBD40)
-- Como a coluna foi adicionada na USBD31 mas os inserts não a contemplam,
-- forçamos a localização inicial de todos os vagões para Leixões (ID 50).
UPDATE Wagon SET current_facility_id = 50;

COMMIT;

-- ===============================================================
-- CORREÇÕES DE INTEGRIDADE E REQUISITOS (USBD31/37/33)
-- ===============================================================

-- 1. Povoar Wagon_Model_Gauge (Crítico para USBD37)
-- Como os modelos foram inseridos neste script, a tabela de associação estava vazia.
INSERT INTO Wagon_Model_Gauge (wagon_model_id, gauge_value)
SELECT id, gauge FROM Wagon_Model
WHERE NOT EXISTS (SELECT 1 FROM Wagon_Model_Gauge WHERE wagon_model_id = Wagon_Model.id);

-- 2. Garantir que não há campos nulos em colunas obrigatórias de negócio (Train Length)
UPDATE Train SET max_length_m = 400 WHERE max_length_m IS NULL;

-- 3. Atualização da localização dos vagões (já tinhas, mas reforço a importância)
UPDATE Wagon SET current_facility_id = 50 WHERE current_facility_id IS NULL;

COMMIT;

