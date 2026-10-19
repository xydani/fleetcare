-- Periodic roadworthiness inspection (Italian "revisione").
CREATE TABLE inspection (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    vehicle_id      BIGINT       NOT NULL,
    inspection_date DATE         NOT NULL,
    expiry_date     DATE         NOT NULL,
    outcome         VARCHAR(20)  NOT NULL,
    notes           VARCHAR(500),
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT fk_inspection_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicle (id) ON DELETE CASCADE,
    CONSTRAINT ck_inspection_outcome CHECK (outcome IN ('PASSED', 'REPEAT', 'SUSPENDED')),
    CONSTRAINT ck_inspection_dates CHECK (expiry_date > inspection_date)
);

CREATE INDEX idx_inspection_vehicle_date ON inspection (vehicle_id, inspection_date DESC);
