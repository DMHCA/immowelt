-- V2__add_id_primary_key_to_estates.sql
-- This migration adds a new primary key column 'id' to the 'estates' table
-- and keeps the existing 'global_object_key' unique

-- 1. Add the new column 'id' as BIGSERIAL (auto-increment)
ALTER TABLE estates
ADD COLUMN id BIGSERIAL;

-- 2. Set the new 'id' column as primary key
ALTER TABLE estates
ADD CONSTRAINT estates_id_pkey PRIMARY KEY (id);

-- 3. Ensure 'global_object_key' remains unique but is no longer the primary key
ALTER TABLE estates
ADD CONSTRAINT estates_global_object_key_unique UNIQUE (global_object_key);