-- Demo data, loaded only with the "demo" profile.
-- Dates are relative to the day the migration runs, so there are always
-- inspections that are expired or about to expire.

INSERT INTO owner (first_name, last_name, email, phone) VALUES
    ('Giulia',     'Bianchi',  'giulia.bianchi@example.it',   '+39 333 104 2231'),
    ('Marco',      'Ferrari',  'marco.ferrari@example.it',    '+39 347 552 8810'),
    ('Sara',       'Romano',   'sara.romano@example.it',      '+39 320 991 4472'),
    ('Luca',       'Colombo',  'luca.colombo@example.it',     '+39 338 210 7765'),
    ('Francesca',  'Ricci',    'francesca.ricci@example.it',  NULL),
    ('Alessandro', 'Marino',   'alessandro.marino@example.it','+39 349 870 1123'),
    ('Chiara',     'Greco',    'chiara.greco@example.it',     '+39 331 445 9087'),
    ('Davide',     'Bruno',    'davide.bruno@example.it',     '+39 366 302 5518'),
    ('Elena',      'Gallo',    'elena.gallo@example.it',      NULL),
    ('Matteo',     'Conti',    'matteo.conti@example.it',     '+39 340 118 6634'),
    ('Valentina',  'De Luca',  'valentina.deluca@example.it', '+39 329 774 2290'),
    ('Logistica Nord', 'S.r.l.', 'flotta@logisticanord.example.it', '+39 02 4455 1200');

-- The owner is looked up with a subquery, not a join: a join may reorder the rows, while a plain
-- scan of VALUES keeps the listed order, so ids are stable (1 = FG482KL ... 30 = GP036XE).
-- The maintenance service's demo data refers to vehicles by these ids.
INSERT INTO vehicle (plate, brand, model, production_year, fuel_type, registration_date, owner_id)
SELECT v.plate, v.brand, v.model, v.production_year, v.fuel_type, v.registration_date,
       (SELECT o.id FROM owner o WHERE o.email = v.email)
FROM (VALUES
    ('FG482KL', 'Fiat',       'Panda',       2017, 'PETROL',   DATE '2017-04-12', 'giulia.bianchi@example.it'),
    ('GA915TR', 'Toyota',     'Yaris',       2020, 'HYBRID',   DATE '2020-09-03', 'giulia.bianchi@example.it'),
    ('EZ307MB', 'Volkswagen', 'Golf',        2015, 'DIESEL',   DATE '2015-06-22', 'marco.ferrari@example.it'),
    ('GK228PS', 'Tesla',      'Model 3',     2022, 'ELECTRIC', DATE '2022-02-18', 'marco.ferrari@example.it'),
    ('FL561CN', 'Renault',    'Clio',        2018, 'PETROL',   DATE '2018-01-30', 'sara.romano@example.it'),
    ('DY774HJ', 'Fiat',       'Punto',       2011, 'LPG',      DATE '2011-07-08', 'luca.colombo@example.it'),
    ('GE650WX', 'Dacia',      'Sandero',     2021, 'LPG',      DATE '2021-05-14', 'luca.colombo@example.it'),
    ('FS139RA', 'Ford',       'Focus',       2019, 'DIESEL',   DATE '2019-03-25', 'francesca.ricci@example.it'),
    ('FB803ZE', 'Peugeot',    '208',         2016, 'PETROL',   DATE '2016-10-11', 'alessandro.marino@example.it'),
    ('GH442LV', 'Jeep',       'Renegade',    2022, 'HYBRID',   DATE '2022-06-07', 'alessandro.marino@example.it'),
    ('FN290KB', 'Alfa Romeo', 'Giulietta',   2018, 'DIESEL',   DATE '2018-05-19', 'chiara.greco@example.it'),
    ('EP517GT', 'Lancia',     'Ypsilon',     2013, 'CNG',      DATE '2013-11-02', 'davide.bruno@example.it'),
    ('GC118DM', 'Hyundai',    'Kona',        2020, 'ELECTRIC', DATE '2020-12-15', 'elena.gallo@example.it'),
    ('FV975NP', 'Kia',        'Sportage',    2019, 'DIESEL',   DATE '2019-08-28', 'matteo.conti@example.it'),
    ('GM603SF', 'BMW',        'Serie 1',     2023, 'PETROL',   DATE '2023-03-09', 'matteo.conti@example.it'),
    ('FD344WC', 'Audi',       'A3',          2016, 'DIESEL',   DATE '2016-02-04', 'valentina.deluca@example.it'),
    ('GN871BH', 'Fiat',       '500',         2024, 'HYBRID',   DATE '2024-01-22', 'valentina.deluca@example.it'),
    ('FR208LT', 'Fiat',       'Doblò',       2019, 'CNG',      DATE '2019-02-13', 'flotta@logisticanord.example.it'),
    ('FR209LT', 'Fiat',       'Doblò',       2019, 'CNG',      DATE '2019-02-13', 'flotta@logisticanord.example.it'),
    ('FX562KD', 'Ford',       'Transit',     2019, 'DIESEL',   DATE '2019-11-06', 'flotta@logisticanord.example.it'),
    ('GB730PA', 'Renault',    'Kangoo',      2020, 'DIESEL',   DATE '2020-04-17', 'flotta@logisticanord.example.it'),
    ('GB731PA', 'Renault',    'Kangoo',      2020, 'DIESEL',   DATE '2020-04-17', 'flotta@logisticanord.example.it'),
    ('GF415MV', 'Volkswagen', 'Caddy',       2021, 'CNG',      DATE '2021-09-29', 'flotta@logisticanord.example.it'),
    ('GJ084RC', 'Peugeot',    'Partner',     2022, 'ELECTRIC', DATE '2022-01-12', 'flotta@logisticanord.example.it'),
    ('GL299TN', 'Toyota',     'Proace City', 2022, 'DIESEL',   DATE '2022-10-03', 'flotta@logisticanord.example.it'),
    ('FT655BW', 'Fiat',       'Ducato',      2019, 'DIESEL',   DATE '2019-06-18', 'flotta@logisticanord.example.it'),
    ('EV148ZS', 'Volkswagen', 'Polo',        2014, 'PETROL',   DATE '2014-09-15', 'sara.romano@example.it'),
    ('GD567HK', 'Toyota',     'C-HR',        2021, 'HYBRID',   DATE '2021-02-26', 'chiara.greco@example.it'),
    ('FA921MR', 'Dacia',      'Duster',      2016, 'LPG',      DATE '2016-03-31', 'davide.bruno@example.it'),
    ('GP036XE', 'Kia',        'Niro',        2024, 'ELECTRIC', DATE '2024-07-10', 'elena.gallo@example.it')
) AS v (plate, brand, model, production_year, fuel_type, registration_date, email);

-- One reading every quarter for the last two years (never before registration).
-- Each vehicle drives a different yearly distance; the small per-reading jitter
-- is always smaller than a quarter's driving, so km never decrease over time.
INSERT INTO mileage_reading (vehicle_id, reading_date, km)
SELECT r.vehicle_id, r.reading_date, r.km
FROM (
    SELECT v.id AS vehicle_id,
           v.registration_date,
           CURRENT_DATE - q * 91 AS reading_date,
           ((CURRENT_DATE - v.registration_date) * (8000 + v.id * 1973 % 17000) / 365)
               - q * (8000 + v.id * 1973 % 17000) / 4
               - (q * v.id * 131) % 700 AS km
    FROM vehicle v
    CROSS JOIN generate_series(0, 7) AS q
) r
WHERE r.reading_date >= r.registration_date
  AND r.km >= 0;

-- Italian rule: first inspection after 4 years, then every 2 years.
-- Expiry dates are spread from 60 days ago to about a year from now.
WITH latest AS (
    SELECT id AS vehicle_id,
           CURRENT_DATE + (id * 47 % 400 - 60)::int AS expiry_date
    FROM vehicle
    WHERE registration_date <= CURRENT_DATE - INTERVAL '4 years'
)
INSERT INTO inspection (vehicle_id, inspection_date, expiry_date, outcome, notes)
SELECT vehicle_id, (expiry_date - INTERVAL '2 years')::date, expiry_date, 'PASSED', NULL
FROM latest
UNION ALL
SELECT vehicle_id, (expiry_date - INTERVAL '4 years')::date, (expiry_date - INTERVAL '2 years')::date,
       'REPEAT', 'Worn front brake pads, re-inspected within a month'
FROM latest
WHERE vehicle_id % 5 = 0;
