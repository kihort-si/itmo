-- PostgreSQL queries for public transport database

SELECT COUNT(DISTINCT rbs.ID_STOP) AS stop_count
FROM mass_transport.ROUTE_BY_STOPS rbs
JOIN mass_transport.VEHICLE v
    ON rbs.ID_VEHICLE = v.ID_VEHICLE
JOIN mass_transport.DIRECTION d
    ON rbs.ID_DIRECTION = d.ID_DIRECTION
WHERE rbs.ROUTE_NUMBER = 14
  AND v.VEHICLE_NAME = 'АВТОБУС'
  AND d.DIRECTION_TYPE = 'ПРЯМОЕ';

SELECT COUNT(DISTINCT rbs.ID_STOP) AS stop_count
FROM mass_transport.ROUTE_BY_STOPS rbs
JOIN mass_transport.VEHICLE v
    ON rbs.ID_VEHICLE = v.ID_VEHICLE
JOIN mass_transport.DIRECTION d
    ON rbs.ID_DIRECTION = d.ID_DIRECTION
WHERE rbs.ROUTE_NUMBER = 11
  AND v.VEHICLE_NAME = 'ТРОЛЛЕЙБУС'
  AND d.DIRECTION_TYPE = 'ОБРАТНОЕ';

SELECT DISTINCT t.ROUTE_NUMBER
FROM mass_transport.TRACK t
JOIN mass_transport.VEHICLE v
    ON t.ID_VEHICLE = v.ID_VEHICLE
WHERE t.CARRIER_BOARD_NUM = 194155
  AND v.VEHICLE_NAME = 'АВТОБУС';

SELECT COUNT(DISTINCT t.CARRIER_BOARD_NUM) AS vehicle_count
FROM mass_transport.TRACK t
JOIN mass_transport.VEHICLE v
    ON t.ID_VEHICLE = v.ID_VEHICLE
WHERE t.ROUTE_NUMBER = 14
  AND v.VEHICLE_NAME = 'АВТОБУС';

SELECT MIN(rbs.DISTANCE_BACK) AS min_distance
FROM mass_transport.ROUTE_BY_STOPS rbs
JOIN mass_transport.VEHICLE v
    ON rbs.ID_VEHICLE = v.ID_VEHICLE
WHERE rbs.ROUTE_NUMBER = 46
  AND v.VEHICLE_NAME = 'АВТОБУС'
  AND rbs.DISTANCE_BACK > 0;

SELECT DISTINCT v.VEHICLE_NAME
FROM mass_transport.ROUTE_BY_STOPS rbs
JOIN mass_transport.VEHICLE v
    ON rbs.ID_VEHICLE = v.ID_VEHICLE
WHERE rbs.ROUTE_NUMBER = 10;

SELECT s.LATITUDE, s.LONGITUDE
FROM mass_transport.ROUTE_BY_STOPS rbs
JOIN mass_transport.VEHICLE v
    ON rbs.ID_VEHICLE = v.ID_VEHICLE
JOIN mass_transport.DIRECTION d
    ON rbs.ID_DIRECTION = d.ID_DIRECTION
JOIN mass_transport.STOPS s
    ON rbs.ID_STOP = s.ID_STOP
WHERE rbs.ROUTE_NUMBER = 191
  AND v.VEHICLE_NAME = 'АВТОБУС'
  AND d.DIRECTION_TYPE = 'ОБРАТНОЕ'
  AND rbs.STOP_NUMBER = 12;

SELECT rbs.ROUTE_NUMBER, SUM(rbs.DISTANCE_BACK) AS route_length
FROM mass_transport.ROUTE_BY_STOPS rbs
JOIN mass_transport.VEHICLE v
    ON rbs.ID_VEHICLE = v.ID_VEHICLE
JOIN mass_transport.DIRECTION d
    ON rbs.ID_DIRECTION = d.ID_DIRECTION
WHERE v.VEHICLE_NAME = 'АВТОБУС'
  AND d.DIRECTION_TYPE = 'ПРЯМОЕ'
GROUP BY rbs.ROUTE_NUMBER
HAVING SUM(rbs.DISTANCE_BACK) BETWEEN 11800 AND 18550;

SELECT COUNT(*) AS stop_count
FROM mass_transport.STOPS
WHERE STOP_NAME LIKE '%ПРОТОК%';
