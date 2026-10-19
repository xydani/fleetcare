CREATE TABLE mileage_reading (
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    vehicle_id   BIGINT      NOT NULL,
    reading_date DATE        NOT NULL,
    km           INTEGER     NOT NULL,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_mileage_reading_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicle (id) ON DELETE CASCADE,
    CONSTRAINT uk_mileage_reading_vehicle_date UNIQUE (vehicle_id, reading_date),
    CONSTRAINT ck_mileage_reading_km CHECK (km >= 0)
);
