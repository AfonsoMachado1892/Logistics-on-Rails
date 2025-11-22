-- usbd24-data-setup.sql - Versão FINAL E DEFENITIVA

-- =================================================================
-- 1. DESATIVAÇÃO & LIMPEZA FORÇADA
-- =================================================================
-- Desativa constraints para permitir a eliminação total (Resolve ORA-02292)
ALTER TABLE line DISABLE CONSTRAINT FKline57062;
ALTER TABLE line DISABLE CONSTRAINT FKline467100;
ALTER TABLE line DISABLE CONSTRAINT FKline868044;
/

-- Eliminação total de dados de teste (Resolve ORA-00001)
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

-- =================================================================
-- 2. INSERIR CHAVES MÃES (Com Sufixos Únicos)
-- =================================================================

-- MÃES AUXILIARES
INSERT INTO owner (id_owner, short_name, vatnumber) VALUES (101, 'Infra', '100000000');
INSERT INTO operator (id_operator, short_name_operator, vatnumber_operator) VALUES (101, 'CP_US24_F', '999999991');
INSERT INTO operator (id_operator, short_name_operator, vatnumber_operator) VALUES (102, 'MEDWAY_US24_F', '999999992');
INSERT INTO wagon_model (id_model, model_wagon, maker, number_bogies, bogies, lenght, width, height, weight, max_speed, payload, volume, type, bitola)
VALUES (101, 'VAGAO_AUTO_US24_F', 'FABRICANTE', 2, 'BOGIES_STD', 100, 30, 40, 200, 100, 50, 60, 'CARGA', 1668);

-- FACILITIES (Estações)
INSERT INTO facility (id_facility, name) VALUES (101, 'Porto Campanhã US24 F');
INSERT INTO facility (id_facility, name) VALUES (102, 'Lisboa Oriente US24 F');
INSERT INTO facility (id_facility, name) VALUES (103, 'Faro US24 F');
INSERT INTO facility (id_facility, name) VALUES (104, 'Braga US24 F');
/

-- WAGON (Chave Pai Crítica)
INSERT INTO wagon ("number", model, operator, service_year, model_id, Operator_id)
VALUES (9999, 'M_DUMMY_AUTO_F', 'OP_TESTE_F', 2020,
    (SELECT id_model FROM wagon_model WHERE model_wagon = 'VAGAO_AUTO_US24_F'),
    (SELECT Id_operator FROM operator WHERE short_name_operator = 'CP_US24_F'));
/

-- FORÇAR COMMIT via PL/SQL para garantir o Parent Key (Resolve ORA-02291)
BEGIN
COMMIT;
END;
/

-- =================================================================
-- 3. INSERIR FILHOS (TRAINS e COMPOSITION)
-- =================================================================

-- TRAINS (Comboios)
INSERT INTO train (id_train, operator, time, "date", start_id, end_id, freight, locomotive, path, operator_id)
VALUES (100, 1, TO_TIMESTAMP('10:00:00', 'HH24:MI:SS'), DATE '2025-11-22',
    (SELECT id_facility FROM facility WHERE name = 'Porto Campanhã US24 F'),
    (SELECT id_facility FROM facility WHERE name = 'Lisboa Oriente US24 F'),
    'MINÉRIO', 'L_DUMMY_1', 'P_DUMMY_1',
    (SELECT Id_operator FROM operator WHERE short_name_operator = 'CP_US24_F'));

INSERT INTO train (id_train, operator, time, "date", start_id, end_id, freight, locomotive, path, operator_id)
VALUES (101, 2, TO_TIMESTAMP('12:00:00', 'HH24:MI:SS'), DATE '2025-11-22',
        (SELECT id_facility FROM facility WHERE name = 'Lisboa Oriente US24 F'),
        (SELECT id_facility FROM facility WHERE name = 'Faro US24 F'),
        'CEREAIS', 'L_DUMMY_2', 'P_DUMMY_2',
        (SELECT Id_operator FROM operator WHERE short_name_operator = 'MEDWAY_US24_F'));

INSERT INTO train (id_train, operator, time, "date", start_id, end_id, freight, locomotive, path, operator_id)
VALUES (102, 1, TO_TIMESTAMP('14:00:00', 'HH24:MI:SS'), DATE '2025-11-22',
        (SELECT id_facility FROM facility WHERE name = 'Braga US24 F'),
        (SELECT id_facility FROM facility WHERE name = 'Braga US24 F'),
        'PASSAGEIROS', 'L_DUMMY_3', 'P_DUMMY_3',
        (SELECT Id_operator FROM operator WHERE short_name_operator = 'CP_US24_F'));
/

-- TRAIN_COMPOSITION (Referencia o WAGON 9999 e o novo TRAIN)
INSERT INTO train_composition (id_train_composition, trainid_train, wagonnumber, position_in_train, loaded, load_weight)
VALUES (1,
    (SELECT id_train FROM train WHERE freight = 'MINÉRIO' AND start_id = (SELECT id_facility FROM facility WHERE name = 'Porto Campanhã US24 F')),
    9999, 1, HEXTORAW('01'), 50);
/

-- =================================================================
-- 4. ATIVAÇÃO DE CONSTRAINTS & COMMIT FINAL
-- =================================================================
ALTER TABLE line ENABLE CONSTRAINT FKline57062;
ALTER TABLE line ENABLE CONSTRAINT FKline467100;
ALTER TABLE line ENABLE CONSTRAINT FKline868044;
/

COMMIT;
/