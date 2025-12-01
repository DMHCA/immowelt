-- V4__add_availability_column.sql
-- Add availability column to estates and estate_history tables

ALTER TABLE estates
    ADD COLUMN availability VARCHAR(255);

ALTER TABLE estate_history
    ADD COLUMN availability VARCHAR(255);
