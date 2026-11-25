INSERT INTO movies VALUES (1,'The Last Local','Thriller','Marathi'),(2,'Orbit Nine','Science fiction','English'),(3,'Rangmanch','Drama','Hindi') ON CONFLICT DO NOTHING;
INSERT INTO screenings(movie_id,starts_at,auditorium,price)
SELECT m.id, (((now() AT TIME ZONE 'Asia/Kolkata')::date + d) + t) AT TIME ZONE 'Asia/Kolkata',
 'Screen ' || m.id, 120 + 30*m.id
FROM movies m CROSS JOIN generate_series(0,6) d CROSS JOIN (VALUES (time '13:30'),(time '18:30')) AS slots(t)
ON CONFLICT DO NOTHING;
