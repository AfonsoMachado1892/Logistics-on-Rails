/* ==========================================================================
   1. ELIMINAÇÃO DE TABELAS (CLEANUP)
   ========================================================================== */
BEGIN
FOR t IN (SELECT table_name FROM user_tables) LOOP
        EXECUTE IMMEDIATE 'DROP TABLE ' || t.table_name || ' CASCADE CONSTRAINTS';
END LOOP;
EXCEPTION
    WHEN OTHERS THEN NULL;
END;
/

/* ==========================================================================
   2. CRIAÇÃO DE TABELAS (DDL)
   ========================================================================== */

-- Tabela de Instalações/Estações
CREATE TABLE Facility (
                          id NUMBER PRIMARY KEY,
                          name VARCHAR2(100) NOT NULL
);

-- Tabela de Proprietários de Linhas (Owner)
CREATE TABLE Line_Owner (
                            vat_number VARCHAR2(20) PRIMARY KEY,
                            name VARCHAR2(100) NOT NULL,
                            short_name VARCHAR2(20)
);

-- Tabela de Operadores de Comboios (Operator)
CREATE TABLE Operator (
                          vat_number VARCHAR2(20) PRIMARY KEY,
                          name VARCHAR2(100) NOT NULL,
                          short_name VARCHAR2(20)
);

-- Tabela de Linhas Ferroviárias
CREATE TABLE Line (
                      id NUMBER PRIMARY KEY,
                      name VARCHAR2(100),
                      owner_vat VARCHAR2(20),
                      start_facility_id NUMBER,
                      end_facility_id NUMBER,
                      gauge NUMBER,
                      CONSTRAINT fk_line_owner FOREIGN KEY (owner_vat) REFERENCES Line_Owner(vat_number),
                      CONSTRAINT fk_line_start FOREIGN KEY (start_facility_id) REFERENCES Facility(id),
                      CONSTRAINT fk_line_end FOREIGN KEY (end_facility_id) REFERENCES Facility(id)
);

-- Tabela de Segmentos de Linha
CREATE TABLE Segment (
                         id NUMBER PRIMARY KEY,
                         line_id NUMBER NOT NULL,
                         sequence_order NUMBER NOT NULL,
                         electrified CHAR(3) CHECK (electrified IN ('Yes', 'No')),
                         max_weight_kg_m NUMBER,
                         length_m NUMBER,
                         number_tracks NUMBER,
                         siding_position NUMBER, -- Pode ser NULL
                         siding_length NUMBER,   -- Pode ser NULL
                         CONSTRAINT fk_segment_line FOREIGN KEY (line_id) REFERENCES Line(id)
);

-- Tabela de Modelos de Vagões
CREATE TABLE Wagon_Model (
                             id NUMBER PRIMARY KEY,
                             model_name VARCHAR2(50),
                             maker VARCHAR2(50),
                             num_bogies NUMBER,
                             bogies_type VARCHAR2(50),
                             length_mm NUMBER,
                             width_mm NUMBER,
                             height_mm NUMBER,
                             weight_tons NUMBER,
                             max_speed_kmh NUMBER,
                             payload_tons NUMBER,
                             volume_m3 NUMBER,
                             type_desc VARCHAR2(100),
                             gauge NUMBER
);

-- Tabela de Vagões Físicos
CREATE TABLE Wagon (
                       wagon_number VARCHAR2(50) PRIMARY KEY, -- Mantém-se VARCHAR devido aos espaços (ex: "356 3 077")
                       model_id NUMBER NOT NULL,
                       operator_vat VARCHAR2(20),
                       service_start_date DATE,
                       CONSTRAINT fk_wagon_model FOREIGN KEY (model_id) REFERENCES Wagon_Model(id),
                       CONSTRAINT fk_wagon_op FOREIGN KEY (operator_vat) REFERENCES Operator(vat_number)
);

-- Tabela de Modelos de Locomotivas
CREATE TABLE Locomotive_Model (
                                  id NUMBER PRIMARY KEY,
                                  model_name VARCHAR2(50) NOT NULL,
                                  make VARCHAR2(50) NOT NULL,
                                  num_bogies NUMBER,
                                  bogies_type VARCHAR2(50),
                                  power_kw NUMBER,
                                  length_m NUMBER,
                                  width_m NUMBER,
                                  height_m NUMBER,
                                  weight_tons NUMBER,
                                  max_speed_kmh NUMBER,
                                  traction_kn NUMBER,
                                  engine_type VARCHAR2(20) CHECK (UPPER(engine_type) IN ('ELECTRIC', 'DIESEL')),
                                  voltage VARCHAR2(20),
                                  frequency VARCHAR2(20),
                                  fuel_capacity_l NUMBER,
                                  gauge NUMBER
);

-- Tabela de Locomotivas (ALTERADA PARA REFERENCIAR O MODELO - USBD43)
CREATE TABLE Locomotive (
                            loco_number VARCHAR2(50) PRIMARY KEY, -- Pode conter pontos (ex: "335.001")
                            model_id NUMBER NOT NULL,
                            name VARCHAR2(50),
                            service_start_date DATE,
                            operational_speed_kmh NUMBER,
                            operator_vat VARCHAR2(20),
                            CONSTRAINT fk_loco_model FOREIGN KEY (model_id) REFERENCES Locomotive_Model(id),
                            CONSTRAINT fk_loco_op FOREIGN KEY (operator_vat) REFERENCES Operator(vat_number)
);

-- Tabela de Freights (Serviços de Transporte de Carga)
CREATE TABLE Freight (
                         id NUMBER PRIMARY KEY,
                         freight_date DATE,
                         origin_facility_id NUMBER,
                         dest_facility_id NUMBER,
                         CONSTRAINT fk_freight_orig FOREIGN KEY (origin_facility_id) REFERENCES Facility(id),
                         CONSTRAINT fk_freight_dest FOREIGN KEY (dest_facility_id) REFERENCES Facility(id)
);

-- Tabela de Associação: Freight <-> Vagões (Normalização da lista de vagões)
CREATE TABLE Freight_Wagon (
                               freight_id NUMBER,
                               wagon_number VARCHAR2(50),
                               CONSTRAINT pk_freight_wagon PRIMARY KEY (freight_id, wagon_number),
                               CONSTRAINT fk_fw_freight FOREIGN KEY (freight_id) REFERENCES Freight(id),
                               CONSTRAINT fk_fw_wagon FOREIGN KEY (wagon_number) REFERENCES Wagon(wagon_number)
);

-- Tabela de Comboios (Trains)
CREATE TABLE Train (
                       id NUMBER PRIMARY KEY,
                       operator_vat VARCHAR2(20),
                       departure_date DATE,
                       departure_time VARCHAR2(8), -- Formato HH:MM:SS ou usar TIMESTAMP combinado
                       start_facility_id NUMBER,
                       end_facility_id NUMBER,
                       CONSTRAINT fk_train_op FOREIGN KEY (operator_vat) REFERENCES Operator(vat_number),
                       CONSTRAINT fk_train_start FOREIGN KEY (start_facility_id) REFERENCES Facility(id),
                       CONSTRAINT fk_train_end FOREIGN KEY (end_facility_id) REFERENCES Facility(id)
);

-- Tabela de Associação: Comboio <-> Freights (Normalização da lista de Freights)
CREATE TABLE Train_Freight (
                               train_id NUMBER,
                               freight_id NUMBER,
                               CONSTRAINT pk_train_freight PRIMARY KEY (train_id, freight_id),
                               CONSTRAINT fk_tf_train FOREIGN KEY (train_id) REFERENCES Train(id),
                               CONSTRAINT fk_tf_freight FOREIGN KEY (freight_id) REFERENCES Freight(id)
);

-- Tabela de Associação: Comboio <-> Locomotivas (Normalização da lista de Locomotivas)
CREATE TABLE Train_Locomotive (
                                  train_id NUMBER,
                                  loco_number VARCHAR2(50),
                                  CONSTRAINT pk_train_loco PRIMARY KEY (train_id, loco_number),
                                  CONSTRAINT fk_tl_train FOREIGN KEY (train_id) REFERENCES Train(id),
                                  CONSTRAINT fk_tl_loco FOREIGN KEY (loco_number) REFERENCES Locomotive(loco_number)
);

-- Tabela de Associação: Percurso do Comboio (Normalização da lista Path)
CREATE TABLE Train_Path (
                            train_id NUMBER,
                            sequence_order NUMBER,
                            facility_id NUMBER,
                            arrival_time TIMESTAMP, -- Opcional, para uso futuro
                            departure_time TIMESTAMP, -- Opcional, para uso futuro
                            CONSTRAINT pk_train_path PRIMARY KEY (train_id, sequence_order),
                            CONSTRAINT fk_tp_train FOREIGN KEY (train_id) REFERENCES Train(id),
                            CONSTRAINT fk_tp_facility FOREIGN KEY (facility_id) REFERENCES Facility(id)
);

-- 1. Para USBD33: Adicionar limite de comprimento ao comboio
ALTER TABLE Train ADD max_length_m NUMBER;

-- 2. Para USBD38: Tabela de Bitolas (Normalização)
CREATE TABLE Railway_Gauge (
                               gauge_value NUMBER PRIMARY KEY,
                               description VARCHAR2(50)
);
-- Inserir as bitolas padrão do sistema (DML)
INSERT INTO Railway_Gauge (gauge_value, description) VALUES (1668, 'Bitola Ibérica');
INSERT INTO Railway_Gauge (gauge_value, description) VALUES (1435, 'Bitola Standard Europeia');

-- Atualizar FK nas tabelas existentes (Opcional, mas recomendado para integridade)
ALTER TABLE Line ADD CONSTRAINT fk_line_gauge FOREIGN KEY (gauge) REFERENCES Railway_Gauge(gauge_value);
ALTER TABLE Wagon_Model ADD CONSTRAINT fk_model_gauge FOREIGN KEY (gauge) REFERENCES Railway_Gauge(gauge_value);
ALTER TABLE Locomotive_Model ADD CONSTRAINT fk_lm_gauge FOREIGN KEY (gauge) REFERENCES Railway_Gauge(gauge_value);

-- 3. Para USBD37: Suporte a múltiplas bitolas por vagão
CREATE TABLE Wagon_Model_Gauge (
                                   wagon_model_id NUMBER,
                                   gauge_value NUMBER,
                                   CONSTRAINT pk_wmg PRIMARY KEY (wagon_model_id, gauge_value),
                                   CONSTRAINT fk_wmg_model FOREIGN KEY (wagon_model_id) REFERENCES Wagon_Model(id),
                                   CONSTRAINT fk_wmg_gauge FOREIGN KEY (gauge_value) REFERENCES Railway_Gauge(gauge_value)
);
-- Migrar dados atuais para a nova estrutura
INSERT INTO Wagon_Model_Gauge (wagon_model_id, gauge_value)
SELECT id, gauge FROM Wagon_Model;

-- 4. Para USBD36: Tabela de Edifícios
CREATE TABLE Building (
                          id NUMBER GENERATED BY DEFAULT ON NULL AS IDENTITY PRIMARY KEY,
                          facility_id NUMBER NOT NULL,
                          name VARCHAR2(50),
                          type VARCHAR2(50), -- ex: 'Warehouse', 'Terminal', 'Silo'
                          CONSTRAINT fk_building_facility FOREIGN KEY (facility_id) REFERENCES Facility(id)
);

-- 5. Para USBD40: Saber onde o vagão está estacionado
ALTER TABLE Wagon ADD current_facility_id NUMBER;
ALTER TABLE Wagon ADD CONSTRAINT fk_wagon_curr_fac FOREIGN KEY (current_facility_id) REFERENCES Facility(id);

-- (DML) Inicializar localização dos vagões (assumir Leixões ou NULL para começar)
UPDATE Wagon SET current_facility_id = 50;

-- 6. SEQUÊNCIAS: Essenciais para criar novos registos via PL/SQL (USBD35, USBD40)
CREATE SEQUENCE seq_freight_id START WITH 3000 INCREMENT BY 1;
CREATE SEQUENCE seq_facility_id START WITH 100 INCREMENT BY 1;
CREATE SEQUENCE seq_train_id START WITH 6000 INCREMENT BY 1;

COMMIT;