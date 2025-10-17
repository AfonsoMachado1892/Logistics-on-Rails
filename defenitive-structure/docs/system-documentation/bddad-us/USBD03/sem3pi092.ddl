-- ==========================================================
--  CONVERSÃO DE MYSQL → ORACLE 19c (IDENTITY + NOMES VÁLIDOS)
-- ==========================================================

-- === TRAIN ===
CREATE TABLE train (
                       id_train NUMBER GENERATED ALWAYS AS IDENTITY,
                       name_train VARCHAR2(255),
                       CONSTRAINT pk_train PRIMARY KEY (id_train)
);

-- === LOCOMOTIVE ===
CREATE TABLE locomotive (
                            id_locomotive NUMBER GENERATED ALWAYS AS IDENTITY,
                            name_locomotive VARCHAR2(255),
                            size NUMBER,
                            cargo_type NUMBER NOT NULL,
                            CONSTRAINT pk_locomotive PRIMARY KEY (id_locomotive)
);

-- === TRAIN_LOCOMOTIVE ===
CREATE TABLE train_locomotive (
                                  train_id NUMBER NOT NULL,
                                  locomotive_id NUMBER NOT NULL
);

-- === STATION ===
CREATE TABLE station (
                         id_station NUMBER GENERATED ALWAYS AS IDENTITY,
                         name_station VARCHAR2(255),
                         positionx NUMBER,
                         positiony NUMBER,
                         type_station_id NUMBER NOT NULL,
                         CONSTRAINT pk_station PRIMARY KEY (id_station)
);

-- === STATION_TYPE ===
CREATE TABLE station_type (
                              id_station_type NUMBER GENERATED ALWAYS AS IDENTITY,
                              station_type_name VARCHAR2(255),
                              CONSTRAINT pk_station_type PRIMARY KEY (id_station_type)
);

-- === LINE ===
CREATE TABLE line (
                      id_line NUMBER GENERATED ALWAYS AS IDENTITY,
                      name_line VARCHAR2(255),
                      distance_line NUMBER,
                      CONSTRAINT pk_line PRIMARY KEY (id_line)
);

-- === LINE_STATION ===
CREATE TABLE line_station (
                              line_id NUMBER NOT NULL,
                              station_id NUMBER NOT NULL
);

-- === TRAIN_LINE ===
CREATE TABLE train_line (
                            train_id NUMBER NOT NULL,
                            line_id NUMBER NOT NULL
);

-- === WAGON ===
CREATE TABLE wagon (
                       id_wagon NUMBER GENERATED ALWAYS AS IDENTITY,
                       name_wagon VARCHAR2(255),
                       CONSTRAINT pk_wagon PRIMARY KEY (id_wagon)
);

-- === BOX ===
CREATE TABLE box (
                     id_box NUMBER GENERATED ALWAYS AS IDENTITY,
                     box_sku VARCHAR2(255) NOT NULL,
                     quantity NUMBER,
                     expiry_date DATE,
                     received_at DATE,
                     CONSTRAINT pk_box PRIMARY KEY (id_box)
);

-- === WAREHOUSE ===
CREATE TABLE warehouse (
                           id_warehouse NUMBER GENERATED ALWAYS AS IDENTITY,
                           name_warehouse VARCHAR2(255),
                           number_aisles NUMBER,
                           CONSTRAINT pk_warehouse PRIMARY KEY (id_warehouse)
);

-- === AISLES ===
CREATE TABLE aisles (
                        id_aisle NUMBER GENERATED ALWAYS AS IDENTITY,
                        number_bays NUMBER,
                        CONSTRAINT pk_aisles PRIMARY KEY (id_aisle)
);

-- === AISLES_WAREHOUSE ===
CREATE TABLE aisles_warehouse (
                                  aisle_id NUMBER NOT NULL,
                                  warehouse_id NUMBER NOT NULL
);

-- === BAY ===
CREATE TABLE bay (
                     id_bay NUMBER GENERATED ALWAYS AS IDENTITY,
                     box_capacity NUMBER,
                     priority NUMBER,
                     CONSTRAINT pk_bay PRIMARY KEY (id_bay)
);

-- === BAY_AISLES ===
CREATE TABLE bay_aisles (
                            bay_id NUMBER NOT NULL,
                            aisle_id NUMBER NOT NULL
);

-- === PRODUCT ===
CREATE TABLE product (
                         id_product NUMBER GENERATED ALWAYS AS IDENTITY,
                         product_name VARCHAR2(255),
                         category NUMBER,
                         unit VARCHAR2(255),
                         volume NUMBER,
                         unit_weight NUMBER,
                         quantity NUMBER,
                         CONSTRAINT pk_product PRIMARY KEY (id_product)
);

-- === PRODUCT_IN_BOX ===
CREATE TABLE product_in_box (
                                box_id NUMBER NOT NULL,
                                product_id NUMBER NOT NULL
);

-- === CARGO_TYPE ===
CREATE TABLE cargo_type (
                            id_cargo_type NUMBER GENERATED ALWAYS AS IDENTITY,
                            name_cargo_type VARCHAR2(255),
                            CONSTRAINT pk_cargo_type PRIMARY KEY (id_cargo_type)
);

-- ===========================================
-- CHAVES ESTRANGEIRAS
-- ===========================================

ALTER TABLE train_locomotive
    ADD CONSTRAINT fk_train_loco_train FOREIGN KEY (train_id) REFERENCES train (id_train);

ALTER TABLE train_locomotive
    ADD CONSTRAINT fk_train_loco_loco FOREIGN KEY (locomotive_id) REFERENCES locomotive (id_locomotive);

ALTER TABLE station
    ADD CONSTRAINT fk_station_type FOREIGN KEY (type_station_id) REFERENCES station_type (id_station_type);

ALTER TABLE line_station
    ADD CONSTRAINT fk_line_station_line FOREIGN KEY (line_id) REFERENCES line (id_line);

ALTER TABLE line_station
    ADD CONSTRAINT fk_line_station_station FOREIGN KEY (station_id) REFERENCES station (id_station);

ALTER TABLE train_line
    ADD CONSTRAINT fk_train_line_train FOREIGN KEY (train_id) REFERENCES train (id_train);

ALTER TABLE train_line
    ADD CONSTRAINT fk_train_line_line FOREIGN KEY (line_id) REFERENCES line (id_line);

ALTER TABLE aisles_warehouse
    ADD CONSTRAINT fk_aisle_wh_aisle FOREIGN KEY (aisle_id) REFERENCES aisles (id_aisle);

ALTER TABLE aisles_warehouse
    ADD CONSTRAINT fk_aisle_wh_warehouse FOREIGN KEY (warehouse_id) REFERENCES warehouse (id_warehouse);

ALTER TABLE bay_aisles
    ADD CONSTRAINT fk_bay_aisles_bay FOREIGN KEY (bay_id) REFERENCES bay (id_bay);

ALTER TABLE bay_aisles
    ADD CONSTRAINT fk_bay_aisles_aisle FOREIGN KEY (aisle_id) REFERENCES aisles (id_aisle);

ALTER TABLE product_in_box
    ADD CONSTRAINT fk_prod_in_box_box FOREIGN KEY (box_id) REFERENCES box (id_box);

ALTER TABLE product_in_box
    ADD CONSTRAINT fk_prod_in_box_prod FOREIGN KEY (product_id) REFERENCES product (id_product);

ALTER TABLE locomotive
    ADD CONSTRAINT fk_loco_cargo FOREIGN KEY (cargo_type) REFERENCES cargo_type (id_cargo_type);
