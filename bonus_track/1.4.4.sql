-- PostgreSQL queries for public transport database

SELECT GetNumber(10, 7);

SELECT COUNT(id_vehicle) AS bus_count
FROM mass_transport.track
WHERE id_stop = 4522
AND stop_time_real >= '2019-09-09 14:00:00'
AND stop_time_real < '2019-09-09 15:00:00';

SELECT COUNT(*) AS stop_count
FROM mass_transport.stops
WHERE CoordinateDistance(59.9418907, 30.3305378, latitude, longitude) <= 350;