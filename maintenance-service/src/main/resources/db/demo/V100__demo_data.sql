-- Demo data, loaded only with the "demo" profile.
-- Vehicle ids, plates, brands and models match the vehicle service's demo data.
-- Dates are relative to the day the migration runs; every slot falls on a weekday within opening hours.

CREATE TEMPORARY TABLE demo_vehicle (id BIGINT, plate VARCHAR(7), brand VARCHAR(60), model VARCHAR(60)) ON COMMIT DROP;
INSERT INTO demo_vehicle VALUES
    (1, 'FG482KL', 'Fiat', 'Panda'),          (2, 'GA915TR', 'Toyota', 'Yaris'),
    (3, 'EZ307MB', 'Volkswagen', 'Golf'),     (4, 'GK228PS', 'Tesla', 'Model 3'),
    (5, 'FL561CN', 'Renault', 'Clio'),        (6, 'DY774HJ', 'Fiat', 'Punto'),
    (7, 'GE650WX', 'Dacia', 'Sandero'),       (8, 'FS139RA', 'Ford', 'Focus'),
    (9, 'FB803ZE', 'Peugeot', '208'),         (10, 'GH442LV', 'Jeep', 'Renegade'),
    (11, 'FN290KB', 'Alfa Romeo', 'Giulietta'), (12, 'EP517GT', 'Lancia', 'Ypsilon'),
    (13, 'GC118DM', 'Hyundai', 'Kona'),       (14, 'FV975NP', 'Kia', 'Sportage'),
    (15, 'GM603SF', 'BMW', 'Serie 1'),        (16, 'FD344WC', 'Audi', 'A3'),
    (17, 'GN871BH', 'Fiat', '500'),           (18, 'FR208LT', 'Fiat', 'Doblò'),
    (19, 'FR209LT', 'Fiat', 'Doblò'),         (20, 'FX562KD', 'Ford', 'Transit'),
    (21, 'GB730PA', 'Renault', 'Kangoo'),     (22, 'GB731PA', 'Renault', 'Kangoo'),
    (23, 'GF415MV', 'Volkswagen', 'Caddy'),   (24, 'GJ084RC', 'Peugeot', 'Partner'),
    (25, 'GL299TN', 'Toyota', 'Proace City'), (26, 'FT655BW', 'Fiat', 'Ducato'),
    (27, 'EV148ZS', 'Volkswagen', 'Polo'),    (28, 'GD567HK', 'Toyota', 'C-HR'),
    (29, 'FA921MR', 'Dacia', 'Duster'),       (30, 'GP036XE', 'Kia', 'Niro');

-- Service types with their default length in minutes and a base parts cost.
CREATE TEMPORARY TABLE demo_service (n INT, service_type VARCHAR(30), minutes INT, parts NUMERIC) ON COMMIT DROP;
INSERT INTO demo_service VALUES
    (0, 'OIL_CHANGE', 45, 65), (1, 'TYRE_CHANGE', 60, 280), (2, 'BRAKES', 120, 190),
    (3, 'SCHEDULED_SERVICE', 180, 210), (4, 'INSPECTION_PREP', 60, 40), (5, 'REPAIR', 240, 450);

-- Past work: four visits per vehicle over the last ~14 months, about three months apart.
-- A Saturday moves to Monday and a Sunday to Monday; starts between 08:00 and 13:00.
INSERT INTO booking (vehicle_id, vehicle_plate, vehicle_brand, vehicle_model, service_type, starts_at, ends_at, status)
SELECT v.id, v.plate, v.brand, v.model, s.service_type,
       d.day + make_interval(hours => 8 + ((v.id + k) % 6)::int),
       d.day + make_interval(hours => 8 + ((v.id + k) % 6)::int, mins => s.minutes),
       'COMPLETED'
FROM demo_vehicle v
CROSS JOIN generate_series(0, 3) AS k
JOIN demo_service s ON s.n = (v.id * 7 + k * 5) % 6
CROSS JOIN LATERAL (
    SELECT CASE EXTRACT(ISODOW FROM raw)
               WHEN 6 THEN raw + 2
               WHEN 7 THEN raw + 1
               ELSE raw
           END::timestamp AS day
    FROM (SELECT CURRENT_DATE - (20 + 97 * k + 3 * v.id)::int AS raw) r
) d;

-- One intervention per completed booking. Fleet vans (Logistica Nord) use the FLEET plan, the newest
-- cars are still under WARRANTY, everyone else pays STANDARD. Same tariffs as application.yml.
INSERT INTO intervention (booking_id, description, pricing_plan, labour_hours, parts_cost, labour_cost, discount,
                          total_cost, completed_at)
SELECT c.id, c.description, c.plan, c.hours, c.parts, c.labour,
       c.discount, c.parts + c.labour - c.discount, c.ends_at
FROM (
    SELECT p.*,
           CASE p.plan
               WHEN 'FLEET' THEN ROUND((p.parts + p.labour) * 0.15, 2)
               WHEN 'WARRANTY' THEN p.parts
               ELSE 0
           END AS discount
    FROM (
        SELECT b.id, b.ends_at,
               initcap(replace(b.service_type, '_', ' ')) || ' on ' || b.vehicle_brand || ' ' || b.vehicle_model
                   AS description,
               CASE
                   WHEN b.vehicle_id BETWEEN 18 AND 26 THEN 'FLEET'
                   WHEN b.vehicle_id IN (15, 17, 30) THEN 'WARRANTY'
                   ELSE 'STANDARD'
               END AS plan,
               ROUND(s.minutes / 60.0, 2) AS hours,
               s.parts + (b.id * 37) % 90 AS parts,
               ROUND(s.minutes / 60.0 * 48, 2) AS labour
        FROM booking b
        JOIN demo_service s ON s.service_type = b.service_type
        WHERE b.status = 'COMPLETED'
    ) p
) c;

-- A cancelled booking and upcoming ones over the next two weeks.
INSERT INTO booking (vehicle_id, vehicle_plate, vehicle_brand, vehicle_model, service_type, starts_at, ends_at, status,
                     notes)
SELECT v.id, v.plate, v.brand, v.model, s.service_type,
       d.day + make_interval(hours => 9 + (v.id % 5)::int),
       d.day + make_interval(hours => 9 + (v.id % 5)::int, mins => s.minutes),
       CASE WHEN v.id = 2 THEN 'CANCELLED' ELSE 'BOOKED' END,
       CASE WHEN v.id = 2 THEN 'Customer asked to postpone' END
FROM demo_vehicle v
JOIN demo_service s ON s.n = v.id % 6
CROSS JOIN LATERAL (
    SELECT CASE EXTRACT(ISODOW FROM raw)
               WHEN 6 THEN raw + 2
               WHEN 7 THEN raw + 1
               ELSE raw
           END::timestamp AS day
    FROM (SELECT CURRENT_DATE + (1 + v.id % 12)::int AS raw) r
) d
WHERE v.id IN (2, 3, 4, 8, 11, 14, 20, 26);
