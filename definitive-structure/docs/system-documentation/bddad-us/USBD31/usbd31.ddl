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

-- Tabela de Locomotivas
CREATE TABLE Locomotive (
                            loco_number VARCHAR2(50) PRIMARY KEY, -- Pode conter pontos (ex: "335.001")
                            name VARCHAR2(50),
                            make VARCHAR2(50),
                            model VARCHAR2(50),
                            service_start_date DATE,
                            num_bogies NUMBER,
                            bogies_type VARCHAR2(50),
                            power_kw NUMBER,
                            length_m NUMBER,
                            width_m NUMBER,
                            height_m NUMBER,
                            weight_tons NUMBER,
                            max_speed_kmh NUMBER,
                            operational_speed_kmh NUMBER,
                            traction_kn NUMBER,
                            engine_type VARCHAR2(20), -- Electric, Diesel
                            voltage VARCHAR2(20),
                            frequency VARCHAR2(20),
                            operator_vat VARCHAR2(20),
                            gauge NUMBER,
                            fuel_capacity_l NUMBER,
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