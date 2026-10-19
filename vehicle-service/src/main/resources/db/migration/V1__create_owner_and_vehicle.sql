CREATE TABLE owner (
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    first_name VARCHAR(80)  NOT NULL,
    last_name  VARCHAR(80)  NOT NULL,
    email      VARCHAR(160) NOT NULL,
    phone      VARCHAR(30),
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uk_owner_email UNIQUE (email)
);

CREATE TABLE vehicle (
    id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    plate             VARCHAR(7)  NOT NULL,
    brand             VARCHAR(60) NOT NULL,
    model             VARCHAR(60) NOT NULL,
    production_year   INTEGER     NOT NULL,
    fuel_type         VARCHAR(20) NOT NULL,
    registration_date DATE        NOT NULL,
    owner_id          BIGINT      NOT NULL,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uk_vehicle_plate UNIQUE (plate),
    CONSTRAINT fk_vehicle_owner FOREIGN KEY (owner_id) REFERENCES owner (id),
    CONSTRAINT ck_vehicle_production_year CHECK (production_year BETWEEN 1950 AND 2100),
    CONSTRAINT ck_vehicle_fuel_type
        CHECK (fuel_type IN ('PETROL', 'DIESEL', 'LPG', 'CNG', 'HYBRID', 'ELECTRIC'))
);

CREATE INDEX idx_vehicle_owner_id ON vehicle (owner_id);
CREATE INDEX idx_vehicle_brand ON vehicle (brand);
