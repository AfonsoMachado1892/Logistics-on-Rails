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


INSERT INTO Line (ID, Name, Owner, Start ID, Start, End ID, End, Gauge) VALUES (1, 'Ramal São Bento - Campanhã', 'IP', 7, 'Porto São Bento', 5, 'Porto Campanhã', 1668);
INSERT INTO Line (ID, Name, Owner, Start ID, Start, End ID, End, Gauge) VALUES (2, 'Ramal Camapanhã - Nine', 'IP', 5, 'Porto Campanhã', 20, 'Nine', 1668);
INSERT INTO Line (ID, Name, Owner, Start ID, Start, End ID, End, Gauge) VALUES (3, 'Ramal Nine - Barcelos', 'IP', 20, 'Nine', 8, 'Barcelos', 1668);
INSERT INTO Line (ID, Name, Owner, Start ID, Start, End ID, End, Gauge) VALUES (4, 'Ramal Barcelos - Viana', 'IP', 8, 'Barcelos', 17, 'Viana do Castelo', 1668);
INSERT INTO Line (ID, Name, Owner, Start ID, Start, End ID, End, Gauge) VALUES (5, 'Ramal viana - Caminha', 'IP', 17, 'Viana do Castelo', 21, 'Caminha', 1668);
INSERT INTO Line (ID, Name, Owner, Start ID, Start, End ID, End, Gauge) VALUES (6, 'Ramal Caminha - Torre', 'IP', 21, 'Caminha', 16, 'São Pedro da Torre', 1668);
INSERT INTO Line (ID, Name, Owner, Start ID, Start, End ID, End, Gauge) VALUES (7, 'Ramal Torre - Valença', 'IP', 16, 'São Pedro da Torre', 11, 'Valença', 1668);


INSERT INTO Segment (ID, Line, Order, Electrified, Max Weight (kg/m), Length (m), NumberTracks) VALUES (1, 1, 1, 'Yes', 8000, 2618, 4);
INSERT INTO Segment (ID, Line, Order, Electrified, Max Weight (kg/m), Length (m), NumberTracks) VALUES (10, 2, 1, 'Yes', 8000, 29003, 2);
INSERT INTO Segment (ID, Line, Order, Electrified, Max Weight (kg/m), Length (m), NumberTracks) VALUES (11, 2, 2, 'Yes', 8000, 10000, 2);
INSERT INTO Segment (ID, Line, Order, Electrified, Max Weight (kg/m), Length (m), NumberTracks) VALUES (15, 3, 1, 'Yes', 8000, 5286, 2);
INSERT INTO Segment (ID, Line, Order, Electrified, Max Weight (kg/m), Length (m), NumberTracks) VALUES (16, 3, 2, 'Yes', 8000, 6000, 2);
INSERT INTO Segment (ID, Line, Order, Electrified, Max Weight (kg/m), Length (m), NumberTracks) VALUES (14, 4, 1, 'Yes', 8000, 10387, 2);
INSERT INTO Segment (ID, Line, Order, Electrified, Max Weight (kg/m), Length (m), NumberTracks) VALUES (12, 4, 2, 'Yes', 8000, 12000, 2);
INSERT INTO Segment (ID, Line, Order, Electrified, Max Weight (kg/m), Length (m), NumberTracks) VALUES (13, 4, 3, 'Yes', 6400, 8000, 2);
INSERT INTO Segment (ID, Line, Order, Electrified, Max Weight (kg/m), Length (m), NumberTracks) VALUES (20, 5, 1, 'Yes', 8000, 6000, 2);
INSERT INTO Segment (ID, Line, Order, Electrified, Max Weight (kg/m), Length (m), NumberTracks) VALUES (21, 5, 2, 'Yes', 8000, 3000, 2);
INSERT INTO Segment (ID, Line, Order, Electrified, Max Weight (kg/m), Length (m), NumberTracks) VALUES (22, 5, 3, 'Yes', 8000, 15000, 2);
INSERT INTO Segment (ID, Line, Order, Electrified, Max Weight (kg/m), Length (m), NumberTracks) VALUES (25, 6, 1, 'Yes', 8000, 20829, 2);
INSERT INTO Segment (ID, Line, Order, Electrified, Max Weight (kg/m), Length (m), NumberTracks) VALUES (26, 7, 1, 'Yes', 8000, 4264, 2);


INSERT INTO Owner (Name, Short name, VATNumber) VALUES ('Infraestruturas de Portugal, SA', 'IP', 'PT503933813');


INSERT INTO Operator (Name, Short name, VATNumber) VALUES ('Medway - Operador Ferroviário de Mercadorias, S.A', 'Medway', 'PT509017800');


INSERT INTO Locomotive (Number, Name, Make, Model, Service, Number Bogies, Bogies, Power, Length, Width, Height, Weight (t), Max Speed, Operational Speed, Traction (KN), Type, Voltage, Frequency, Operator, Bitola, Combustível (l)) VALUES (5621, 'Inês', 'Siemens', 'Eurosprinter', 1995, 2, 'Bo-Bo', 5600, 19.2, 3.0, 4.375, 87, 220, 70.0, 300, 'Electric', '25 KV', '50 Hz', 'Medway', 1668, NULL);
INSERT INTO Locomotive (Number, Name, Make, Model, Service, Number Bogies, Bogies, Power, Length, Width, Height, Weight (t), Max Speed, Operational Speed, Traction (KN), Type, Voltage, Frequency, Operator, Bitola, Combustível (l)) VALUES (5623, 'Paz', 'Siemens', 'Eurosprinter', 1995, 2, 'Bo-Bo', 5600, 19.2, 3.0, 4.375, 87, 220, 70.0, 300, 'Electric', '25 KV', '50 Hz', 'Medway', 1668, NULL);
INSERT INTO Locomotive (Number, Name, Make, Model, Service, Number Bogies, Bogies, Power, Length, Width, Height, Weight (t), Max Speed, Operational Speed, Traction (KN), Type, Voltage, Frequency, Operator, Bitola, Combustível (l)) VALUES (5630, 'Helena', 'Siemens', 'Eurosprinter', 1996, 2, 'Bo-Bo', 5600, 19.2, 3.0, 4.375, 87, 220, 70.0, 300, 'Electric', '25 KV', '50 Hz', 'Medway', 1668, NULL);
INSERT INTO Locomotive (Number, Name, Make, Model, Service, Number Bogies, Bogies, Power, Length, Width, Height, Weight (t), Max Speed, Operational Speed, Traction (KN), Type, Voltage, Frequency, Operator, Bitola, Combustível (l)) VALUES (1903, 'Eva', '	Sorefame - Alsthom', 'CP 1900', 1981, 2, 'Co-Co', 1623, 19.084, 3.062, 4.31, 117, 100, 42.5, 396, 'Diesel', NULL, NULL, 'Medway', 1668, 4882.0);


INSERT INTO Wagon_Model (ID, Model, Maker, Number Bogies, Bogies, Length, Width, Height, Weight (t), Max Speed, Payload (t), Volume (m3), Type, Bitola) VALUES (1245, 'Tadgs 32 94 082 3', 'Metalsines', 2, 'Duplo', 17240, 3072, 4270.0, 24.0, 120, 56.0, 75.0, 'Cereal wagon', 1668);
INSERT INTO Wagon_Model (ID, Model, Maker, Number Bogies, Bogies, Length, Width, Height, Weight (t), Max Speed, Payload (t), Volume (m3), Type, Bitola) VALUES (1278, 'Tdgs 41 94 074 1', 'Equimetal', 2, 'Duplo', 9640, 3120, 4165.5, 13.8, 100, 26.2, 38.0, 'Cereal wagon', 1668);
INSERT INTO Wagon_Model (ID, Model, Maker, Number Bogies, Bogies, Length, Width, Height, Weight (t), Max Speed, Payload (t), Volume (m3), Type, Bitola) VALUES (1325, 'Gabs 81 94 181 1', 'Sepsa Cometna', 2, 'Duplo', 21700, 3180, 4170.0, 29.8, 100, 50.2, 110.0, 'Covered wagon with sliding door', 1668);
INSERT INTO Wagon_Model (ID, Model, Maker, Number Bogies, Bogies, Length, Width, Height, Weight (t), Max Speed, Payload (t), Volume (m3), Type, Bitola) VALUES (1104, 'Regmms 32 94 356 3', 'Metalsines', 2, 'Duplo', 14040, 3104, 2535.0, 21.2, 120, 60.6, 76.3, 'Container wagon (max 40'''' HC)', 1668);
INSERT INTO Wagon_Model (ID, Model, Maker, Number Bogies, Bogies, Length, Width, Height, Weight (t), Max Speed, Payload (t), Volume (m3), Type, Bitola) VALUES (985, 'Lgs 22 94 441 6', 'Metalsines', 1, 'Simples', 13860, 2850, 1060.0, 11.9, 120, 28.1, 76.3, 'Container wagon (max 40'''' HC)', 1668);
INSERT INTO Wagon_Model (ID, Model, Maker, Number Bogies, Bogies, Length, Width, Height, Weight (t), Max Speed, Payload (t), Volume (m3), Type, Bitola) VALUES (987, 'Sgnss 12 94 455 2', 'Emef', 2, 'Duplo', 18116, 2950, 1030.0, 21.6, 120, 68.4, 76.3, 'Container wagon (max 40'''' HC)', 1668);
INSERT INTO Wagon_Model (ID, Model, Maker, Number Bogies, Bogies, Length, Width, Height, Weight (t), Max Speed, Payload (t), Volume (m3), Type, Bitola) VALUES (988, 'Sgnss 12 94 455 2', 'Emef', 2, 'Duplo', 18116, 2950, 1030.0, 21.6, 120, 68.4, 76.3, 'Container wagon (max 40'''' HC)', 1435);


INSERT INTO Wagon (Number, Model, Operator, Service Year) VALUES ('356 3 077', 'Regmms 32 94 356 3', 'Medway', 1987);
INSERT INTO Wagon (Number, Model, Operator, Service Year) VALUES ('356 3 078', 'Regmms 32 94 356 3', 'Medway', 1987);
INSERT INTO Wagon (Number, Model, Operator, Service Year) VALUES ('356 3 079', 'Regmms 32 94 356 3', 'Medway', 1987);
INSERT INTO Wagon (Number, Model, Operator, Service Year) VALUES ('356 3 080', 'Regmms 32 94 356 3', 'Medway', 1987);
INSERT INTO Wagon (Number, Model, Operator, Service Year) VALUES ('356 3 081', 'Regmms 32 94 356 3', 'Medway', 1987);
INSERT INTO Wagon (Number, Model, Operator, Service Year) VALUES ('356 3 082', 'Regmms 32 94 356 3', 'Medway', 1987);
INSERT INTO Wagon (Number, Model, Operator, Service Year) VALUES ('356 3 083', 'Regmms 32 94 356 3', 'Medway', 1987);
INSERT INTO Wagon (Number, Model, Operator, Service Year) VALUES ('356 3 084', 'Regmms 32 94 356 3', 'Medway', 1987);
INSERT INTO Wagon (Number, Model, Operator, Service Year) VALUES ('356 3 085', 'Regmms 32 94 356 3', 'Medway', 1987);
INSERT INTO Wagon (Number, Model, Operator, Service Year) VALUES ('356 3 086', 'Regmms 32 94 356 3', 'Medway', 1987);
INSERT INTO Wagon (Number, Model, Operator, Service Year) VALUES ('356 3 087', 'Regmms 32 94 356 3', 'Medway', 1987);
INSERT INTO Wagon (Number, Model, Operator, Service Year) VALUES ('356 3 088', 'Regmms 32 94 356 3', 'Medway', 1987);
INSERT INTO Wagon (Number, Model, Operator, Service Year) VALUES ('356 3 089', 'Regmms 32 94 356 3', 'Medway', 1987);
INSERT INTO Wagon (Number, Model, Operator, Service Year) VALUES ('356 3 090', 'Regmms 32 94 356 3', 'Medway', 1987);
INSERT INTO Wagon (Number, Model, Operator, Service Year) VALUES ('356 3 091', 'Regmms 32 94 356 3', 'Medway', 1987);
INSERT INTO Wagon (Number, Model, Operator, Service Year) VALUES ('356 3 092', 'Regmms 32 94 356 3', 'Medway', 1987);
INSERT INTO Wagon (Number, Model, Operator, Service Year) VALUES ('082 3 045', 'Tadgs 32 94 082 3', 'Medway', 1990);
INSERT INTO Wagon (Number, Model, Operator, Service Year) VALUES ('082 3 046', 'Tadgs 32 94 082 3', 'Medway', 1990);
INSERT INTO Wagon (Number, Model, Operator, Service Year) VALUES ('082 3 047', 'Tadgs 32 94 082 3', 'Medway', 1990);
INSERT INTO Wagon (Number, Model, Operator, Service Year) VALUES ('082 3 048', 'Tadgs 32 94 082 3', 'Medway', 1990);
INSERT INTO Wagon (Number, Model, Operator, Service Year) VALUES ('074 1 001', 'Tdgs 41 94 074 1', 'Medway', 1977);
INSERT INTO Wagon (Number, Model, Operator, Service Year) VALUES ('074 1 002', 'Tdgs 41 94 074 1', 'Medway', 1977);
INSERT INTO Wagon (Number, Model, Operator, Service Year) VALUES ('074 1 003', 'Tdgs 41 94 074 1', 'Medway', 1977);
INSERT INTO Wagon (Number, Model, Operator, Service Year) VALUES ('074 1 004', 'Tdgs 41 94 074 1', 'Medway', 1977);
INSERT INTO Wagon (Number, Model, Operator, Service Year) VALUES ('074 1 005', 'Tdgs 41 94 074 1', 'Medway', 1977);
INSERT INTO Wagon (Number, Model, Operator, Service Year) VALUES ('074 1 006', 'Tdgs 41 94 074 1', 'Medway', 1977);
INSERT INTO Wagon (Number, Model, Operator, Service Year) VALUES ('181 1 010', 'Gabs 81 94 181 1', 'Medway', 1977);
INSERT INTO Wagon (Number, Model, Operator, Service Year) VALUES ('181 1 011', 'Gabs 81 94 181 1', 'Medway', 1977);
INSERT INTO Wagon (Number, Model, Operator, Service Year) VALUES ('181 1 012', 'Gabs 81 94 181 1', 'Medway', 1977);
INSERT INTO Wagon (Number, Model, Operator, Service Year) VALUES ('181 1 013', 'Gabs 81 94 181 1', 'Medway', 1977);
INSERT INTO Wagon (Number, Model, Operator, Service Year) VALUES ('181 1 014', 'Gabs 81 94 181 1', 'Medway', 1977);

