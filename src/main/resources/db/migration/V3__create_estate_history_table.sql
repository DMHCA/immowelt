-- V3__create_estate_history_table.sql
-- Create the estate_history table with the same columns as estates
-- and link to the main estate via foreign key

CREATE TABLE estate_history (
                                id BIGSERIAL PRIMARY KEY,
                                estate_id BIGINT NOT NULL,
                                global_object_key VARCHAR(255),
                                headline VARCHAR(255),
                                estate_type VARCHAR(255),
                                expose_url VARCHAR(255),
                                living_area DOUBLE PRECISION,
                                image VARCHAR(255),
                                imagehd VARCHAR(255),
                                city VARCHAR(255),
                                zip VARCHAR(255),
                                show_map BOOLEAN,
                                street VARCHAR(255),
                                price_name VARCHAR(255),
                                price_value VARCHAR(255),
                                rooms INTEGER,
                                apartment_layout_url VARCHAR(255),
                                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                CONSTRAINT fk_estate FOREIGN KEY (estate_id) REFERENCES estates(id)
);
