-- ==============================================================
-- CONFIGURAÇÃO DA SESSÃO
-- ==============================================================
ALTER SESSION SET NLS_DATE_FORMAT = 'YYYY-MM-DD';
ALTER SESSION SET NLS_TIMESTAMP_FORMAT = 'YYYY-MM-DD HH24:MI:SS';

-- ==============================================================
-- 1. INFRAESTRUTURA (Bitolas, Donos, Operadores, Estações)
-- ==============================================================

-- GAUGE
INSERT INTO Gauge (id_gauge, width) VALUES (1, 1668);
INSERT INTO Gauge (id_gauge, width) VALUES (2, 1435);
INSERT INTO Gauge (id_gauge, width) VALUES (3, 4882);
INSERT INTO Gauge (id_gauge, width) VALUES (4, 6700);

-- OWNER
INSERT INTO owner (id_owner, short_name, vatnumber) VALUES (1, 'IP', 'PT503933813');

-- OPERATOR
INSERT INTO operator (Id_operator, short_name_operator, vatnumber_operator) VALUES (1, 'Medway', 'PT509017800');
INSERT INTO operator (Id_operator, short_name_operator, vatnumber_operator) VALUES (2, 'Captrain', 'PT507832388');

-- FACILITY (Estações)
INSERT INTO facility (id_facility, name) VALUES (1, 'São Romão');
INSERT INTO facility (id_facility, name) VALUES (2, 'Tamel');
INSERT INTO facility (id_facility, name) VALUES (3, 'Senhora das Dores');
INSERT INTO facility (id_facility, name) VALUES (4, 'Lousado');
INSERT INTO facility (id_facility, name) VALUES (5, 'Porto Campanhã');
INSERT INTO facility (id_facility, name) VALUES (6, 'Leandro');
INSERT INTO facility (id_facility, name) VALUES (7, 'Porto São Bento');
INSERT INTO facility (id_facility, name) VALUES (8, 'Barcelos');
INSERT INTO facility (id_facility, name) VALUES (9, 'Vila Nova da Cerveira');
INSERT INTO facility (id_facility, name) VALUES (10, 'Midões');
INSERT INTO facility (id_facility, name) VALUES (11, 'Valença');
INSERT INTO facility (id_facility, name) VALUES (12, 'Darque');
INSERT INTO facility (id_facility, name) VALUES (13, 'Contumil');
INSERT INTO facility (id_facility, name) VALUES (14, 'Ermesinde');
INSERT INTO facility (id_facility, name) VALUES (15, 'São Frutuoso');
INSERT INTO facility (id_facility, name) VALUES (16, 'São Pedro da Torre');
INSERT INTO facility (id_facility, name) VALUES (17, 'Viana do Castelo');
INSERT INTO facility (id_facility, name) VALUES (18, 'Famalicão');
INSERT INTO facility (id_facility, name) VALUES (19, 'Barroselas');
INSERT INTO facility (id_facility, name) VALUES (20, 'Nine');
INSERT INTO facility (id_facility, name) VALUES (21, 'Caminha');
INSERT INTO facility (id_facility, name) VALUES (22, 'Carvalha');
INSERT INTO facility (id_facility, name) VALUES (23, 'Carreço');
INSERT INTO facility (id_facility, name) VALUES (43, 'São Gemil');
INSERT INTO facility (id_facility, name) VALUES (45, 'São Mamede de Infesta');
INSERT INTO facility (id_facility, name) VALUES (48, 'Leça do Balio');
INSERT INTO facility (id_facility, name) VALUES (50, 'Leixões');

-- ==============================================================
-- 2. REDE (Linhas e Segmentos)
-- ==============================================================

-- LINE
INSERT INTO line (id_line, name_line, owner, start_id, start_name, end_id, end_name, gauge, ownerid_owner) VALUES (1, 'Ramal São Bento - Campanhã', 'IP', 7, 'Porto São Bento', 5, 'Porto Campanhã', 1668, 1);
INSERT INTO line (id_line, name_line, owner, start_id, start_name, end_id, end_name, gauge, ownerid_owner) VALUES (2, 'Ramal Campanhã - Contumil', 'IP', 5, 'Porto Campanhã', 13, 'Contumil', 1668, 1);
INSERT INTO line (id_line, name_line, owner, start_id, start_name, end_id, end_name, gauge, ownerid_owner) VALUES (3, 'Ramal Contumil - Nine', 'IP', 13, 'Contumil', 20, 'Nine', 1668, 1);
INSERT INTO line (id_line, name_line, owner, start_id, start_name, end_id, end_name, gauge, ownerid_owner) VALUES (4, 'Ramal Nine - Barcelos', 'IP', 20, 'Nine', 8, 'Barcelos', 1668, 1);
INSERT INTO line (id_line, name_line, owner, start_id, start_name, end_id, end_name, gauge, ownerid_owner) VALUES (5, 'Ramal Barcelos - Darque', 'IP', 8, 'Barcelos', 12, 'Darque', 1668, 1);
INSERT INTO line (id_line, name_line, owner, start_id, start_name, end_id, end_name, gauge, ownerid_owner) VALUES (6, 'Ramal Darque - Viana', 'IP', 12, 'Darque', 17, 'Viana do Castelo', 1668, 1);
INSERT INTO line (id_line, name_line, owner, start_id, start_name, end_id, end_name, gauge, ownerid_owner) VALUES (7, 'Ramal Viana - Caminha', 'IP', 17, 'Viana do Castelo', 21, 'Caminha', 1668, 1);
INSERT INTO line (id_line, name_line, owner, start_id, start_name, end_id, end_name, gauge, ownerid_owner) VALUES (8, 'Ramal Caminha - Torre', 'IP', 21, 'Caminha', 16, 'São Pedro da Torre', 1668, 1);
INSERT INTO line (id_line, name_line, owner, start_id, start_name, end_id, end_name, gauge, ownerid_owner) VALUES (9, 'Ramal Torre - Valença', 'IP', 16, 'São Pedro da Torre', 11, 'Valença', 1668, 1);
INSERT INTO line (id_line, name_line, owner, start_id, start_name, end_id, end_name, gauge, ownerid_owner) VALUES (21, 'Ramal Contumil - São Gemil', 'IP', 13, 'Contumil', 43, 'São Gemil', 1668, 1);
INSERT INTO line (id_line, name_line, owner, start_id, start_name, end_id, end_name, gauge, ownerid_owner) VALUES (22, 'Ramal São Gemil - São Mamede', 'IP', 43, 'São Gemil', 45, 'São Mamede de Infesta', 1668, 1);
INSERT INTO line (id_line, name_line, owner, start_id, start_name, end_id, end_name, gauge, ownerid_owner) VALUES (23, 'Ramal São Mamede - Leça', 'IP', 45, 'São Mamede de Infesta', 48, 'Leça do Balio', 1668, 1);
INSERT INTO line (id_line, name_line, owner, start_id, start_name, end_id, end_name, gauge, ownerid_owner) VALUES (24, 'Ramal Leça - Leixões', 'IP', 48, 'Leça do Balio', 50, 'Leixões', 1668, 1);

-- GAUGE_LINE
INSERT INTO Gauge_line (gauge_id, line_id) SELECT 1, id_line FROM line;

-- SEGMENT
INSERT INTO segment (id_segment, line_id, "order", eletrified, weight, length, number_tracks, siding_position, siding_lenght) VALUES (1, 1, 1, UTL_RAW.CAST_TO_RAW('Yes'), 8000, 2618, '4', 0, 0);
INSERT INTO segment (id_segment, line_id, "order", eletrified, weight, length, number_tracks, siding_position, siding_lenght) VALUES (3, 2, 1, UTL_RAW.CAST_TO_RAW('Yes'), 8000, 2443, '4', 0, 0);
INSERT INTO segment (id_segment, line_id, "order", eletrified, weight, length, number_tracks, siding_position, siding_lenght) VALUES (10, 3, 1, UTL_RAW.CAST_TO_RAW('Yes'), 8000, 26560, '2', 0, 0);
INSERT INTO segment (id_segment, line_id, "order", eletrified, weight, length, number_tracks, siding_position, siding_lenght) VALUES (11, 3, 2, UTL_RAW.CAST_TO_RAW('Yes'), 8000, 10000, '2', 0, 0);
INSERT INTO segment (id_segment, line_id, "order", eletrified, weight, length, number_tracks, siding_position, siding_lenght) VALUES (15, 4, 1, UTL_RAW.CAST_TO_RAW('Yes'), 8000, 5286, '2', 0, 0);
INSERT INTO segment (id_segment, line_id, "order", eletrified, weight, length, number_tracks, siding_position, siding_lenght) VALUES (16, 4, 2, UTL_RAW.CAST_TO_RAW('Yes'), 8000, 6000, '2', 0, 0);
INSERT INTO segment (id_segment, line_id, "order", eletrified, weight, length, number_tracks, siding_position, siding_lenght) VALUES (14, 5, 1, UTL_RAW.CAST_TO_RAW('Yes'), 8000, 10387, '2', 0, 0);
INSERT INTO segment (id_segment, line_id, "order", eletrified, weight, length, number_tracks, siding_position, siding_lenght) VALUES (12, 5, 2, UTL_RAW.CAST_TO_RAW('Yes'), 8000, 12000, '2', 0, 0);
INSERT INTO segment (id_segment, line_id, "order", eletrified, weight, length, number_tracks, siding_position, siding_lenght) VALUES (13, 5, 3, UTL_RAW.CAST_TO_RAW('Yes'), 8000, 3100, '2', 0, 0);
INSERT INTO segment (id_segment, line_id, "order", eletrified, weight, length, number_tracks, siding_position, siding_lenght) VALUES (20, 6, 1, UTL_RAW.CAST_TO_RAW('Yes'), 6400, 4890, '2', 0, 0);
INSERT INTO segment (id_segment, line_id, "order", eletrified, weight, length, number_tracks, siding_position, siding_lenght) VALUES (18, 7, 1, UTL_RAW.CAST_TO_RAW('Yes'), 8000, 6000, '1', 0, 0);
INSERT INTO segment (id_segment, line_id, "order", eletrified, weight, length, number_tracks, siding_position, siding_lenght) VALUES (21, 7, 2, UTL_RAW.CAST_TO_RAW('Yes'), 8000, 5000, '1', 2000, 864);
INSERT INTO segment (id_segment, line_id, "order", eletrified, weight, length, number_tracks, siding_position, siding_lenght) VALUES (22, 7, 3, UTL_RAW.CAST_TO_RAW('Yes'), 8000, 12000, '1', 0, 0);
INSERT INTO segment (id_segment, line_id, "order", eletrified, weight, length, number_tracks, siding_position, siding_lenght) VALUES (25, 8, 1, UTL_RAW.CAST_TO_RAW('Yes'), 8000, 20829, '1', 11000, 268);
INSERT INTO segment (id_segment, line_id, "order", eletrified, weight, length, number_tracks, siding_position, siding_lenght) VALUES (26, 9, 1, UTL_RAW.CAST_TO_RAW('Yes'), 8000, 4264, '1', 0, 0);
INSERT INTO segment (id_segment, line_id, "order", eletrified, weight, length, number_tracks, siding_position, siding_lenght) VALUES (30, 21, 1, UTL_RAW.CAST_TO_RAW('Yes'), 8000, 3883, '2', 0, 0);
INSERT INTO segment (id_segment, line_id, "order", eletrified, weight, length, number_tracks, siding_position, siding_lenght) VALUES (31, 22, 1, UTL_RAW.CAST_TO_RAW('Yes'), 8400, 1174, '2', 0, 0);
INSERT INTO segment (id_segment, line_id, "order", eletrified, weight, length, number_tracks, siding_position, siding_lenght) VALUES (32, 22, 2, UTL_RAW.CAST_TO_RAW('Yes'), 8000, 2534, '2', 0, 0);
INSERT INTO segment (id_segment, line_id, "order", eletrified, weight, length, number_tracks, siding_position, siding_lenght) VALUES (33, 23, 1, UTL_RAW.CAST_TO_RAW('Yes'), 8000, 1566, '2', 0, 0);
INSERT INTO segment (id_segment, line_id, "order", eletrified, weight, length, number_tracks, siding_position, siding_lenght) VALUES (34, 23, 2, UTL_RAW.CAST_TO_RAW('Yes'), 8000, 1453, '2', 0, 0);
INSERT INTO segment (id_segment, line_id, "order", eletrified, weight, length, number_tracks, siding_position, siding_lenght) VALUES (35, 24, 1, UTL_RAW.CAST_TO_RAW('Yes'), 8100, 3597, '2', 0, 0);
INSERT INTO segment (id_segment, line_id, "order", eletrified, weight, length, number_tracks, siding_position, siding_lenght) VALUES (36, 24, 2, UTL_RAW.CAST_TO_RAW('Yes'), 8000, 4334, '2', 0, 0);

-- ==============================================================
-- 3. MATERIAL CIRCULANTE - LOCOMOTIVAS
-- ==============================================================

-- LOCOMOTIVE
INSERT INTO locomotive (id_locomotive, name, make, service, number_bogies, bogies, power, length, width, height, weight, max_speed, operational_speed, traction, type, voltage, frequency, operator, bitola, combustivel) VALUES (5621, 'Inês', 'Siemens', 1995, 2, 'Bo-Bo', 5600, 19, 3, 4, 87, 220, 70, 300, 'Electric', 25000, 50, 'Medway', 1668, NULL);
INSERT INTO locomotive (id_locomotive, name, make, service, number_bogies, bogies, power, length, width, height, weight, max_speed, operational_speed, traction, type, voltage, frequency, operator, bitola, combustivel) VALUES (5623, 'Paz', 'Siemens', 1995, 2, 'Bo-Bo', 5600, 19, 3, 4, 87, 220, 70, 300, 'Electric', 25000, 50, 'Medway', 1668, NULL);
INSERT INTO locomotive (id_locomotive, name, make, service, number_bogies, bogies, power, length, width, height, weight, max_speed, operational_speed, traction, type, voltage, frequency, operator, bitola, combustivel) VALUES (5630, 'Helena', 'Siemens', 1996, 2, 'Bo-Bo', 5600, 19, 3, 4, 87, 220, 70, 300, 'Electric', 25000, 50, 'Medway', 1668, NULL);
INSERT INTO locomotive (id_locomotive, name, make, service, number_bogies, bogies, power, length, width, height, weight, max_speed, operational_speed, traction, type, voltage, frequency, operator, bitola, combustivel) VALUES (1903, 'Eva', 'Sorefame - Alsthom', 1981, 2, 'Co-Co', 1623, 19, 3, 4, 117, 100, 43, 396, 'Diesel', NULL, NULL, 'Medway', 1668, 4882);
INSERT INTO locomotive (id_locomotive, name, make, service, number_bogies, bogies, power, length, width, height, weight, max_speed, operational_speed, traction, type, voltage, frequency, operator, bitola, combustivel) VALUES (5034, 'Adriana', 'Stadler', 2017, 3, 'Co-Co', 3178, 23, 3000, 4, 124, 120, 100, 400, 'Diesel', NULL, NULL, 'Medway', 1668, 6700);
INSERT INTO locomotive (id_locomotive, name, make, service, number_bogies, bogies, power, length, width, height, weight, max_speed, operational_speed, traction, type, voltage, frequency, operator, bitola, combustivel) VALUES (5036, 'Marina', 'Stadler', 2017, 3, 'Co-Co', 3178, 23, 3000, 4, 124, 120, 100, 400, 'Diesel', NULL, NULL, 'Medway', 1668, 6700);
INSERT INTO locomotive (id_locomotive, name, make, service, number_bogies, bogies, power, length, width, height, weight, max_speed, operational_speed, traction, type, voltage, frequency, operator, bitola, combustivel) VALUES (335001, '335.001', 'Stadler', 2019, 3, 'Co-Co', 3178, 23, 3000, 4, 124, 120, 100, 400, 'Diesel', NULL, NULL, 'Captrain', 1668, 6700);
INSERT INTO locomotive (id_locomotive, name, make, service, number_bogies, bogies, power, length, width, height, weight, max_speed, operational_speed, traction, type, voltage, frequency, operator, bitola, combustivel) VALUES (335003, '335.003', 'Stadler', 2019, 3, 'Co-Co', 3178, 23, 3000, 4, 124, 120, 100, 400, 'Diesel', NULL, NULL, 'Captrain', 1668, 6700);

-- LOCOMOTIVE_OPERATOR
INSERT INTO locomotive_operator (operator_id, locomotive_id, short_name_operator) VALUES (1, 5621, 1);
INSERT INTO locomotive_operator (operator_id, locomotive_id, short_name_operator) VALUES (1, 5623, 1);
INSERT INTO locomotive_operator (operator_id, locomotive_id, short_name_operator) VALUES (1, 5630, 1);
INSERT INTO locomotive_operator (operator_id, locomotive_id, short_name_operator) VALUES (1, 1903, 1);
INSERT INTO locomotive_operator (operator_id, locomotive_id, short_name_operator) VALUES (1, 5034, 1);
INSERT INTO locomotive_operator (operator_id, locomotive_id, short_name_operator) VALUES (1, 5036, 1);
INSERT INTO locomotive_operator (operator_id, locomotive_id, short_name_operator) VALUES (2, 335001, 2);
INSERT INTO locomotive_operator (operator_id, locomotive_id, short_name_operator) VALUES (2, 335003, 2);

-- LOCOMOTIVE_GAUGE
INSERT INTO locomotive_Gauge (locomotive_id, Gauge_id) SELECT id_locomotive, 1 FROM locomotive;

-- ==============================================================
-- 4. MATERIAL CIRCULANTE - VAGÕES
-- ==============================================================

-- WAGON MODEL
INSERT INTO wagon_model (id_model, model_wagon, maker, number_bogies, bogies, lenght, width, height, weight, max_speed, payload, volume, type, bitola) VALUES (1245, 'Tadgs 32 94 082 3', 'Metalsines', 2, 'Duplo', 17240, 3072, 4270, 24, 120, 56, 75, 'Cereal wagon', 1668);
INSERT INTO wagon_model (id_model, model_wagon, maker, number_bogies, bogies, lenght, width, height, weight, max_speed, payload, volume, type, bitola) VALUES (1278, 'Tdgs 41 94 074 1', 'Equimetal', 2, 'Duplo', 9640, 3120, 4166, 14, 100, 26, 38, 'Cereal wagon', 1668);
INSERT INTO wagon_model (id_model, model_wagon, maker, number_bogies, bogies, lenght, width, height, weight, max_speed, payload, volume, type, bitola) VALUES (1325, 'Gabs 81 94 181 1', 'Sepsa Comer', 2, 'Duplo', 21700, 3180, 4170, 30, 100, 50, 110, 'Covered wagon with sliding door', 1668);
INSERT INTO wagon_model (id_model, model_wagon, maker, number_bogies, bogies, lenght, width, height, weight, max_speed, payload, volume, type, bitola) VALUES (1104, 'Regmms 32 94 356 3', 'Metalsines', 2, 'Duplo', 14040, 3104, 2535, 21, 120, 61, 76, 'Container wagon (max 40 HC)', 1668);
INSERT INTO wagon_model (id_model, model_wagon, maker, number_bogies, bogies, lenght, width, height, weight, max_speed, payload, volume, type, bitola) VALUES (985, 'Lgs 22 94 441 6', 'Metalsines', 2, 'Simples', 13860, 2850, 1060, 12, 120, 28, 76, 'Container wagon (max 40 HC)', 1668);
INSERT INTO wagon_model (id_model, model_wagon, maker, number_bogies, bogies, lenght, width, height, weight, max_speed, payload, volume, type, bitola) VALUES (987, 'Sgnss 12 94 455 2', 'Emef', 2, 'Duplo', 18116, 2950, 1030, 22, 120, 68, 76, 'Container wagon (max 40 HC)', 1668);
INSERT INTO wagon_model (id_model, model_wagon, maker, number_bogies, bogies, lenght, width, height, weight, max_speed, payload, volume, type, bitola) VALUES (988, 'Sgnss 12 94 455 2', 'Emef', 2, 'Duplo', 18116, 2950, 1030, 22, 120, 68, 76, 'Container wagon (max 40 HC)', 1435);
INSERT INTO wagon_model (id_model, model_wagon, maker, number_bogies, bogies, lenght, width, height, weight, max_speed, payload, volume, type, bitola) VALUES (1525, 'Zaes 81 94 788', 'Equimetal', 2, 'Duplo', 13800, 2950, 4226, 23, 120, 57, 65, 'Biodiesel wagon', 1668);
INSERT INTO wagon_model (id_model, model_wagon, maker, number_bogies, bogies, lenght, width, height, weight, max_speed, payload, volume, type, bitola) VALUES (1523, 'Zaes 81 94 788', 'Equimetal', 2, 'Duplo', 13800, 2950, 4226, 23, 120, 57, 65, 'Biodiesel wagon', 1435);
INSERT INTO wagon_model (id_model, model_wagon, maker, number_bogies, bogies, lenght, width, height, weight, max_speed, payload, volume, type, bitola) VALUES (1212, 'Kbs 41 94 333', 'Simmering', 2, 'Simples', 14020, 2842, 3300, 14, 100, 26, 63, 'Wood wagon', 1668);

-- WAGON
-- Nota: Usamos "number" entre aspas porque assim foi definido na tabela corrigida
INSERT INTO wagon ("number", model, operator, service_year, model_id, Operator_id) VALUES (3563077, 'Regmms 32 94 356 3', 'Medway', 1987, 1104, 1);
INSERT INTO wagon ("number", model, operator, service_year, model_id, Operator_id) VALUES (3563078, 'Regmms 32 94 356 3', 'Medway', 1987, 1104, 1);
INSERT INTO wagon ("number", model, operator, service_year, model_id, Operator_id) VALUES (3563079, 'Regmms 32 94 356 3', 'Medway', 1987, 1104, 1);
INSERT INTO wagon ("number", model, operator, service_year, model_id, Operator_id) VALUES (3563080, 'Regmms 32 94 356 3', 'Medway', 1987, 1104, 1);
INSERT INTO wagon ("number", model, operator, service_year, model_id, Operator_id) VALUES (3563081, 'Regmms 32 94 356 3', 'Medway', 1987, 1104, 1);
INSERT INTO wagon ("number", model, operator, service_year, model_id, Operator_id) VALUES (3563082, 'Regmms 32 94 356 3', 'Medway', 1987, 1104, 1);
INSERT INTO wagon ("number", model, operator, service_year, model_id, Operator_id) VALUES (3563083, 'Regmms 32 94 356 3', 'Medway', 1987, 1104, 1);
INSERT INTO wagon ("number", model, operator, service_year, model_id, Operator_id) VALUES (3563084, 'Regmms 32 94 356 3', 'Medway', 1987, 1104, 1);
INSERT INTO wagon ("number", model, operator, service_year, model_id, Operator_id) VALUES (3563085, 'Regmms 32 94 356 3', 'Medway', 1987, 1104, 1);
INSERT INTO wagon ("number", model, operator, service_year, model_id, Operator_id) VALUES (3563086, 'Regmms 32 94 356 3', 'Medway', 1987, 1104, 1);
INSERT INTO wagon ("number", model, operator, service_year, model_id, Operator_id) VALUES (3563087, 'Regmms 32 94 356 3', 'Medway', 1987, 1104, 1);
INSERT INTO wagon ("number", model, operator, service_year, model_id, Operator_id) VALUES (3563088, 'Regmms 32 94 356 3', 'Medway', 1987, 1104, 1);
INSERT INTO wagon ("number", model, operator, service_year, model_id, Operator_id) VALUES (3563089, 'Regmms 32 94 356 3', 'Medway', 1987, 1104, 1);
INSERT INTO wagon ("number", model, operator, service_year, model_id, Operator_id) VALUES (3563090, 'Regmms 32 94 356 3', 'Medway', 1987, 1104, 1);
INSERT INTO wagon ("number", model, operator, service_year, model_id, Operator_id) VALUES (3563091, 'Regmms 32 94 356 3', 'Medway', 1987, 1104, 1);
INSERT INTO wagon ("number", model, operator, service_year, model_id, Operator_id) VALUES (3563092, 'Regmms 32 94 356 3', 'Medway', 1987, 1104, 1);
INSERT INTO wagon ("number", model, operator, service_year, model_id, Operator_id) VALUES (0823045, 'Tadgs 32 94 082 3', 'Medway', 1990, 1245, 1);
INSERT INTO wagon ("number", model, operator, service_year, model_id, Operator_id) VALUES (0823046, 'Tadgs 32 94 082 3', 'Medway', 1990, 1245, 1);
INSERT INTO wagon ("number", model, operator, service_year, model_id, Operator_id) VALUES (0823047, 'Tadgs 32 94 082 3', 'Medway', 1990, 1245, 1);
INSERT INTO wagon ("number", model, operator, service_year, model_id, Operator_id) VALUES (0823048, 'Tadgs 32 94 082 3', 'Medway', 1990, 1245, 1);
INSERT INTO wagon ("number", model, operator, service_year, model_id, Operator_id) VALUES (0741001, 'Tdgs 41 94 074 1', 'Medway', 1977, 1278, 1);
INSERT INTO wagon ("number", model, operator, service_year, model_id, Operator_id) VALUES (0741002, 'Tdgs 41 94 074 1', 'Medway', 1977, 1278, 1);
INSERT INTO wagon ("number", model, operator, service_year, model_id, Operator_id) VALUES (0741003, 'Tdgs 41 94 074 1', 'Medway', 1977, 1278, 1);
INSERT INTO wagon ("number", model, operator, service_year, model_id, Operator_id) VALUES (0741004, 'Tdgs 41 94 074 1', 'Medway', 1977, 1278, 1);
INSERT INTO wagon ("number", model, operator, service_year, model_id, Operator_id) VALUES (0741005, 'Tdgs 41 94 074 1', 'Medway', 1977, 1278, 1);
INSERT INTO wagon ("number", model, operator, service_year, model_id, Operator_id) VALUES (0741006, 'Tdgs 41 94 074 1', 'Medway', 1977, 1278, 1);
INSERT INTO wagon ("number", model, operator, service_year, model_id, Operator_id) VALUES (1811010, 'Gabs 81 94 181 1', 'Medway', 1977, 1325, 1);
INSERT INTO wagon ("number", model, operator, service_year, model_id, Operator_id) VALUES (1811011, 'Gabs 81 94 181 1', 'Medway', 1977, 1325, 1);
INSERT INTO wagon ("number", model, operator, service_year, model_id, Operator_id) VALUES (1811012, 'Gabs 81 94 181 1', 'Medway', 1977, 1325, 1);
INSERT INTO wagon ("number", model, operator, service_year, model_id, Operator_id) VALUES (1811013, 'Gabs 81 94 181 1', 'Medway', 1977, 1325, 1);
INSERT INTO wagon ("number", model, operator, service_year, model_id, Operator_id) VALUES (1811014, 'Gabs 81 94 181 1', 'Medway', 1977, 1325, 1);
INSERT INTO wagon ("number", model, operator, service_year, model_id, Operator_id) VALUES (3330001, 'Kbs 41 94 333', 'Medway', 2005, 1212, 1);
INSERT INTO wagon ("number", model, operator, service_year, model_id, Operator_id) VALUES (3330002, 'Kbs 41 94 333', 'Medway', 2005, 1212, 1);
INSERT INTO wagon ("number", model, operator, service_year, model_id, Operator_id) VALUES (3330003, 'Kbs 41 94 333', 'Medway', 2005, 1212, 1);
INSERT INTO wagon ("number", model, operator, service_year, model_id, Operator_id) VALUES (3330004, 'Kbs 41 94 333', 'Medway', 2005, 1212, 1);
INSERT INTO wagon ("number", model, operator, service_year, model_id, Operator_id) VALUES (3330005, 'Kbs 41 94 333', 'Medway', 2005, 1212, 1);
INSERT INTO wagon ("number", model, operator, service_year, model_id, Operator_id) VALUES (3330006, 'Kbs 41 94 333', 'Medway', 2005, 1212, 1);
INSERT INTO wagon ("number", model, operator, service_year, model_id, Operator_id) VALUES (3330007, 'Kbs 41 94 333', 'Medway', 2005, 1212, 1);
INSERT INTO wagon ("number", model, operator, service_year, model_id, Operator_id) VALUES (3330008, 'Kbs 41 94 333', 'Medway', 2005, 1212, 1);
INSERT INTO wagon ("number", model, operator, service_year, model_id, Operator_id) VALUES (3330009, 'Kbs 41 94 333', 'Medway', 2005, 1212, 1);
INSERT INTO wagon ("number", model, operator, service_year, model_id, Operator_id) VALUES (3330010, 'Kbs 41 94 333', 'Medway', 2005, 1212, 1);

-- WAGON_GAUGE
INSERT INTO wagon_Gauge (wagonnumber, Gaugeid_gauge) SELECT "number", 1 FROM wagon;

-- ==============================================================
-- 5. OPERAÇÕES E SERVIÇOS
-- ==============================================================

-- FREIGHTS
INSERT INTO freights (id_freights, "date", origin, facility, destination, facility_final, wagon) VALUES (2001, TO_DATE('2025-10-03', 'YYYY-MM-DD'), 50, 50, 12, 12, '333 0 001, 333 0 002, 333 0 004, 333 0 005, 333 0 006');
INSERT INTO freights (id_freights, "date", origin, facility, destination, facility_final, wagon) VALUES (2002, TO_DATE('2025-10-03', 'YYYY-MM-DD'), 13, 13, 11, 11, '356 3 089');
INSERT INTO freights (id_freights, "date", origin, facility, destination, facility_final, wagon) VALUES (2003, TO_DATE('2025-10-03', 'YYYY-MM-DD'), 50, 50, 11, 11, '181 1 011, 181 1 012');
INSERT INTO freights (id_freights, "date", origin, facility, destination, facility_final, wagon) VALUES (2004, TO_DATE('2025-10-03', 'YYYY-MM-DD'), 50, 50, 21, 21, '181 1 013');
INSERT INTO freights (id_freights, "date", origin, facility, destination, facility_final, wagon) VALUES (2005, TO_DATE('2025-10-03', 'YYYY-MM-DD'), 20, 20, 11, 11, '356 3 077, 356 3 078, 356 3 079, 356 3 080');
INSERT INTO freights (id_freights, "date", origin, facility, destination, facility_final, wagon) VALUES (2006, TO_DATE('2025-10-03', 'YYYY-MM-DD'), 12, 12, 50, 50, '333 0 003, 333 0 007');
INSERT INTO freights (id_freights, "date", origin, facility, destination, facility_final, wagon) VALUES (2007, TO_DATE('2025-10-03', 'YYYY-MM-DD'), 50, 50, 5, 5, '356 3 090, 356 3 091, 356 3 092');
INSERT INTO freights (id_freights, "date", origin, facility, destination, facility_final, wagon) VALUES (2050, TO_DATE('2025-10-06', 'YYYY-MM-DD'), 12, 12, 50, 50, '333 0 001, 333 0 002, 333 0 004, 333 0 005, 333 0 006');
INSERT INTO freights (id_freights, "date", origin, facility, destination, facility_final, wagon) VALUES (2051, TO_DATE('2025-10-06', 'YYYY-MM-DD'), 11, 11, 50, 50, '181 1 011, 181 1 012, 356 3 077, 356 3 078, 356 3 079, 356 3 080');

-- TRAIN
INSERT INTO train (id_train, operator, time, "date", start_id, end_id, freight, locomotive, path, operator_id) VALUES (5421, 1, TO_TIMESTAMP('2025-10-03 09:45:00', 'YYYY-MM-DD HH24:MI:SS'), TO_DATE('2025-10-03', 'YYYY-MM-DD'), 50, 11, '2001, 2002, 2003, 2004, 2005', '5621, 5623', '50, 48, 45, 43, 13, 20, 8, 12, 17, 21, 16, 11', 1);
INSERT INTO train (id_train, operator, time, "date", start_id, end_id, freight, locomotive, path, operator_id) VALUES (5435, 1, TO_TIMESTAMP('2025-10-03 18:00:00', 'YYYY-MM-DD HH24:MI:SS'), TO_DATE('2025-10-03', 'YYYY-MM-DD'), 11, 50, '2006', '5623', '11, 16, 21, 17, 12, 8, 20, 13, 43, 45, 48, 50', 1);
INSERT INTO train (id_train, operator, time, "date", start_id, end_id, freight, locomotive, path, operator_id) VALUES (5437, 1, TO_TIMESTAMP('2025-10-06 10:00:00', 'YYYY-MM-DD HH24:MI:SS'), TO_DATE('2025-10-06', 'YYYY-MM-DD'), 11, 50, '2050, 2051', '5621', '11, 16, 21, 17, 12, 8, 20, 13, 43, 45, 48, 50', 1);

-- TRAIN COMPOSITION
INSERT INTO train_composition (id_train_composition, trainid_train, wagonnumber, position_in_train, loaded, load_weight) VALUES (1, 5421, 3330001, 1, UTL_RAW.CAST_TO_RAW('Yes'), 56);
INSERT INTO train_composition (id_train_composition, trainid_train, wagonnumber, position_in_train, loaded, load_weight) VALUES (2, 5421, 3330002, 2, UTL_RAW.CAST_TO_RAW('Yes'), 56);
INSERT INTO train_composition (id_train_composition, trainid_train, wagonnumber, position_in_train, loaded, load_weight) VALUES (3, 5421, 3330004, 3, UTL_RAW.CAST_TO_RAW('Yes'), 56);
INSERT INTO train_composition (id_train_composition, trainid_train, wagonnumber, position_in_train, loaded, load_weight) VALUES (4, 5421, 3330005, 4, UTL_RAW.CAST_TO_RAW('Yes'), 56);
INSERT INTO train_composition (id_train_composition, trainid_train, wagonnumber, position_in_train, loaded, load_weight) VALUES (5, 5421, 3330006, 5, UTL_RAW.CAST_TO_RAW('Yes'), 56);
INSERT INTO train_composition (id_train_composition, trainid_train, wagonnumber, position_in_train, loaded, load_weight) VALUES (6, 5421, 3563089, 6, UTL_RAW.CAST_TO_RAW('Yes'), 60);
INSERT INTO train_composition (id_train_composition, trainid_train, wagonnumber, position_in_train, loaded, load_weight) VALUES (7, 5421, 1811011, 7, UTL_RAW.CAST_TO_RAW('Yes'), 50);
INSERT INTO train_composition (id_train_composition, trainid_train, wagonnumber, position_in_train, loaded, load_weight) VALUES (8, 5421, 1811012, 8, UTL_RAW.CAST_TO_RAW('Yes'), 50);
INSERT INTO train_composition (id_train_composition, trainid_train, wagonnumber, position_in_train, loaded, load_weight) VALUES (9, 5421, 1811013, 9, UTL_RAW.CAST_TO_RAW('Yes'), 50);
INSERT INTO train_composition (id_train_composition, trainid_train, wagonnumber, position_in_train, loaded, load_weight) VALUES (10, 5421, 3563077, 10, UTL_RAW.CAST_TO_RAW('Yes'), 60);
INSERT INTO train_composition (id_train_composition, trainid_train, wagonnumber, position_in_train, loaded, load_weight) VALUES (11, 5421, 3563078, 11, UTL_RAW.CAST_TO_RAW('Yes'), 60);
INSERT INTO train_composition (id_train_composition, trainid_train, wagonnumber, position_in_train, loaded, load_weight) VALUES (12, 5421, 3563079, 12, UTL_RAW.CAST_TO_RAW('Yes'), 60);
INSERT INTO train_composition (id_train_composition, trainid_train, wagonnumber, position_in_train, loaded, load_weight) VALUES (13, 5421, 3563080, 13, UTL_RAW.CAST_TO_RAW('Yes'), 60);
INSERT INTO train_composition (id_train_composition, trainid_train, wagonnumber, position_in_train, loaded, load_weight) VALUES (14, 5435, 3330003, 1, UTL_RAW.CAST_TO_RAW('Yes'), 56);
INSERT INTO train_composition (id_train_composition, trainid_train, wagonnumber, position_in_train, loaded, load_weight) VALUES (15, 5435, 3330007, 2, UTL_RAW.CAST_TO_RAW('Yes'), 56);
INSERT INTO train_composition (id_train_composition, trainid_train, wagonnumber, position_in_train, loaded, load_weight) VALUES (16, 5437, 3330001, 1, UTL_RAW.CAST_TO_RAW('Yes'), 56);
INSERT INTO train_composition (id_train_composition, trainid_train, wagonnumber, position_in_train, loaded, load_weight) VALUES (17, 5437, 3330002, 2, UTL_RAW.CAST_TO_RAW('Yes'), 56);
INSERT INTO train_composition (id_train_composition, trainid_train, wagonnumber, position_in_train, loaded, load_weight) VALUES (18, 5437, 3330004, 3, UTL_RAW.CAST_TO_RAW('Yes'), 56);
INSERT INTO train_composition (id_train_composition, trainid_train, wagonnumber, position_in_train, loaded, load_weight) VALUES (19, 5437, 3330005, 4, UTL_RAW.CAST_TO_RAW('Yes'), 56);
INSERT INTO train_composition (id_train_composition, trainid_train, wagonnumber, position_in_train, loaded, load_weight) VALUES (20, 5437, 3330006, 5, UTL_RAW.CAST_TO_RAW('Yes'), 56);
INSERT INTO train_composition (id_train_composition, trainid_train, wagonnumber, position_in_train, loaded, load_weight) VALUES (21, 5437, 1811011, 6, UTL_RAW.CAST_TO_RAW('Yes'), 50);
INSERT INTO train_composition (id_train_composition, trainid_train, wagonnumber, position_in_train, loaded, load_weight) VALUES (22, 5437, 1811012, 7, UTL_RAW.CAST_TO_RAW('Yes'), 50);
INSERT INTO train_composition (id_train_composition, trainid_train, wagonnumber, position_in_train, loaded, load_weight) VALUES (23, 5437, 3563077, 8, UTL_RAW.CAST_TO_RAW('Yes'), 60);
INSERT INTO train_composition (id_train_composition, trainid_train, wagonnumber, position_in_train, loaded, load_weight) VALUES (24, 5437, 3563078, 9, UTL_RAW.CAST_TO_RAW('Yes'), 60);
INSERT INTO train_composition (id_train_composition, trainid_train, wagonnumber, position_in_train, loaded, load_weight) VALUES (25, 5437, 3563079, 10, UTL_RAW.CAST_TO_RAW('Yes'), 60);
INSERT INTO train_composition (id_train_composition, trainid_train, wagonnumber, position_in_train, loaded, load_weight) VALUES (26, 5437, 3563080, 11, UTL_RAW.CAST_TO_RAW('Yes'), 60);

COMMIT;