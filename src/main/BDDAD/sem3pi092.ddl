CREATE TABLE train (
  id_train   int(10) NOT NULL AUTO_INCREMENT, 
  name_train varchar(255), 
  PRIMARY KEY (id_train));
CREATE TABLE locomotive (
  id_locomotive   int(10) NOT NULL AUTO_INCREMENT, 
  name_locomotive varchar(255), 
  `size`          int(10), 
  cargo_type      int(10) NOT NULL, 
  PRIMARY KEY (id_locomotive));
CREATE TABLE train_locomotive (
  train_id      int(10) NOT NULL, 
  locomotive_id int(10) NOT NULL);
CREATE TABLE station (
  id_station      int(10) NOT NULL AUTO_INCREMENT, 
  name_station    varchar(255), 
  positionx       int(10), 
  positiony       int(10), 
  type_station_id int(10) NOT NULL, 
  PRIMARY KEY (id_station));
CREATE TABLE `station type` (
  id_station_type   int(10) NOT NULL AUTO_INCREMENT, 
  station_type_name varchar(255), 
  PRIMARY KEY (id_station_type));
CREATE TABLE line (
  id_line       int(10) NOT NULL AUTO_INCREMENT, 
  name_line     varchar(255), 
  distance_line int(10), 
  PRIMARY KEY (id_line));
CREATE TABLE line_station (
  line_id    int(10) NOT NULL, 
  station_id int(10) NOT NULL);
CREATE TABLE train_line (
  train_id int(10) NOT NULL, 
  line_id  int(10) NOT NULL);
CREATE TABLE wagon (
  id_wagon   int(10) NOT NULL AUTO_INCREMENT, 
  name_wagon varchar(255), 
  PRIMARY KEY (id_wagon));
CREATE TABLE box (
  id_box      int(10) NOT NULL AUTO_INCREMENT, 
  box_sku     varchar(255) NOT NULL, 
  quantity    int(10), 
  expiry_date int(10), 
  received_at int(10), 
  PRIMARY KEY (id_box));
CREATE TABLE warehouse (
  id_warehouse   int(10) NOT NULL AUTO_INCREMENT, 
  name_warehouse int(10), 
  number_aisles  int(10), 
  PRIMARY KEY (id_warehouse));
CREATE TABLE aisles (
  id_aisle    int(10) NOT NULL AUTO_INCREMENT, 
  number_bays int(10), 
  PRIMARY KEY (id_aisle));
CREATE TABLE aisles_warehouse (
  aisle_id     int(10) NOT NULL, 
  warehouse_id int(10) NOT NULL);
CREATE TABLE bay (
  id_bay       int(10) NOT NULL AUTO_INCREMENT, 
  box_capacity int(10), 
  priority     int(10), 
  PRIMARY KEY (id_bay));
CREATE TABLE bay_aisles (
  bay_id   int(10) NOT NULL, 
  aisle_id int(10) NOT NULL);
CREATE TABLE product (
  id_product   int(10) NOT NULL AUTO_INCREMENT, 
  product_name varchar(255), 
  category     int(10), 
  unit         varchar(255), 
  volume       int(10), 
  unit_weight  int(10), 
  quantity     int(10), 
  PRIMARY KEY (id_product));
CREATE TABLE product_in_box (
  box_id     int(10) NOT NULL, 
  product_id int(10) NOT NULL);
CREATE TABLE cargo_type (
  id_cargo_type   int(10) NOT NULL AUTO_INCREMENT, 
  name_cargo_type int(10), 
  PRIMARY KEY (id_cargo_type));
ALTER TABLE train_locomotive ADD CONSTRAINT FKtrain_loco821349 FOREIGN KEY (train_id) REFERENCES train (id_train);
ALTER TABLE train_locomotive ADD CONSTRAINT FKtrain_loco757643 FOREIGN KEY (locomotive_id) REFERENCES locomotive (id_locomotive);
ALTER TABLE station ADD CONSTRAINT FKstation441479 FOREIGN KEY (type_station_id) REFERENCES `station type` (id_station_type);
ALTER TABLE line_station ADD CONSTRAINT FKline_stati667513 FOREIGN KEY (line_id) REFERENCES line (id_line);
ALTER TABLE line_station ADD CONSTRAINT FKline_stati555473 FOREIGN KEY (station_id) REFERENCES station (id_station);
ALTER TABLE train_line ADD CONSTRAINT FKtrain_line662461 FOREIGN KEY (train_id) REFERENCES train (id_train);
ALTER TABLE train_line ADD CONSTRAINT FKtrain_line528541 FOREIGN KEY (line_id) REFERENCES line (id_line);
ALTER TABLE aisles_warehouse ADD CONSTRAINT FKaisles_war153699 FOREIGN KEY (aisle_id) REFERENCES aisles (id_aisle);
ALTER TABLE aisles_warehouse ADD CONSTRAINT FKaisles_war782225 FOREIGN KEY (warehouse_id) REFERENCES warehouse (id_warehouse);
ALTER TABLE bay_aisles ADD CONSTRAINT FKbay_aisles219303 FOREIGN KEY (bay_id) REFERENCES bay (id_bay);
ALTER TABLE bay_aisles ADD CONSTRAINT FKbay_aisles777794 FOREIGN KEY (aisle_id) REFERENCES aisles (id_aisle);
ALTER TABLE product_in_box ADD CONSTRAINT FKproduct_in924261 FOREIGN KEY (box_id) REFERENCES box (id_box);
ALTER TABLE product_in_box ADD CONSTRAINT FKproduct_in969770 FOREIGN KEY (product_id) REFERENCES product (id_product);
ALTER TABLE locomotive ADD CONSTRAINT FKlocomotive114177 FOREIGN KEY (cargo_type) REFERENCES cargo_type (id_cargo_type);
