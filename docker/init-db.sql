-- One database per service, both on the same PostgreSQL instance.
-- Runs only on the first start, when the data volume is empty.
CREATE DATABASE vehicles_db;
CREATE DATABASE maintenance_db;
