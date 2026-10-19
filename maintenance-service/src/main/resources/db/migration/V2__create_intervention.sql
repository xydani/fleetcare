CREATE TABLE intervention (
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    booking_id   BIGINT        NOT NULL,
    description  VARCHAR(1000) NOT NULL,
    pricing_plan VARCHAR(20)   NOT NULL,
    labour_hours NUMERIC(5, 2) NOT NULL,
    parts_cost   NUMERIC(10, 2) NOT NULL,
    labour_cost  NUMERIC(10, 2) NOT NULL,
    discount     NUMERIC(10, 2) NOT NULL,
    total_cost   NUMERIC(10, 2) NOT NULL,
    completed_at TIMESTAMP     NOT NULL,
    created_at   TIMESTAMPTZ   NOT NULL DEFAULT now(),
    CONSTRAINT fk_intervention_booking FOREIGN KEY (booking_id) REFERENCES booking (id),
    CONSTRAINT uk_intervention_booking UNIQUE (booking_id),
    CONSTRAINT ck_intervention_pricing_plan CHECK (pricing_plan IN ('STANDARD', 'WARRANTY', 'FLEET')),
    CONSTRAINT ck_intervention_amounts CHECK (
        labour_hours >= 0 AND parts_cost >= 0 AND labour_cost >= 0 AND discount >= 0 AND total_cost >= 0)
);

CREATE INDEX ix_intervention_completed_at ON intervention (completed_at);
