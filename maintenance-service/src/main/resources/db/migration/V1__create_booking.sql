-- btree_gist lets a GiST index combine plain equality (vehicle_id) with range overlap.
CREATE EXTENSION IF NOT EXISTS btree_gist;

CREATE TABLE booking (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    vehicle_id    BIGINT       NOT NULL,
    vehicle_plate VARCHAR(7)   NOT NULL,
    vehicle_brand VARCHAR(60)  NOT NULL,
    vehicle_model VARCHAR(60)  NOT NULL,
    service_type  VARCHAR(30)  NOT NULL,
    starts_at     TIMESTAMP    NOT NULL,
    ends_at       TIMESTAMP    NOT NULL,
    status        VARCHAR(20)  NOT NULL,
    notes         VARCHAR(500),
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT ck_booking_service_type CHECK (service_type IN
        ('OIL_CHANGE', 'TYRE_CHANGE', 'BRAKES', 'SCHEDULED_SERVICE', 'INSPECTION_PREP', 'REPAIR')),
    CONSTRAINT ck_booking_status CHECK (status IN ('BOOKED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED')),
    CONSTRAINT ck_booking_time_range CHECK (ends_at > starts_at),
    -- Last line of defence against two concurrent requests booking the same vehicle twice:
    -- the service checks first to return a clear 409, the database guarantees it.
    CONSTRAINT ex_booking_vehicle_overlap EXCLUDE USING gist (
        vehicle_id WITH =,
        tsrange(starts_at, ends_at) WITH &&
    ) WHERE (status IN ('BOOKED', 'IN_PROGRESS'))
);

CREATE INDEX ix_booking_starts_at ON booking (starts_at);
