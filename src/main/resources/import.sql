INSERT INTO owner ( name, email) VALUES ('Ana Pop', 'ana.pop@example.com');
INSERT INTO owner (name, email) VALUES ('Bogdan Ionescu', 'bogdan.ionescu@example.com');

INSERT INTO car (vin, make, model, year_of_manufacture, owner_id) VALUES ('VIN12345', 'Dacia', 'Logan', 2018, 1);
INSERT INTO car (vin, make, model, year_of_manufacture, owner_id) VALUES ('VIN67890', 'VW', 'Golf', 2021, 2);
INSERT INTO car (vin, make, model, year_of_manufacture, owner_id) VALUES ('VIN678s0', 'VW', 'Golf', 2021, 2);
INSERT INTO car (vin, make, model, year_of_manufacture, owner_id) VALUES ('VIN678s0', 'VW', 'Golf', 2021, 2);
INSERT INTO car (vin, make, model, year_of_manufacture, owner_id) VALUES ('VIN678s0', 'VW', 'Golf', 2021, 2);


INSERT INTO insurancepolicy (car_id, provider, start_date, end_date) VALUES (1, 'Allianz', DATE '2024-01-01', DATE '2024-12-31');
INSERT INTO insurancepolicy (car_id, provider, start_date, end_date) VALUES (1, 'Groupama', DATE '2025-01-01', DATE '2026-01-01');
INSERT INTO insurancepolicy (car_id, provider, start_date, end_date) VALUES (2, 'Allianz', DATE '2025-03-01', DATE '2025-09-30');
INSERT INTO insurancepolicy (car_id, provider, start_date, end_date) VALUES (3, 'Allianz', DATE '2025-01-01', DATE '2025-09-30');

INSERT INTO claim (car_id, claim_date, description, amount) VALUES (1, DATE '2024-02-15', 'Minor scratch', 350.00);
INSERT INTO claim (car_id, claim_date, description, amount) VALUES (1, DATE '2024-06-20', 'Broken side mirror', 120.50);
INSERT INTO claim (car_id, claim_date, description, amount) VALUES (2, DATE '2024-08-10', 'Broken Windshield', 800.25);

-- I removed the manual index insert because When inserting new data dynamically
-- into the tables, it wants to start from index 1 and we get an error


