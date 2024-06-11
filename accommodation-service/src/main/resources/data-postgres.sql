INSERT INTO public."bookit-accommodation" (name, location, filters, minguests, maxguests, pricetype)
VALUES ('Ocean View Apartment', 'Miami Beach, FL', 'wifi,free parking,kitchen', 1, 4, 'price per unit');

INSERT INTO priceadjustmentdate (date, price)
VALUES
('2024-06-10', 200),
('2024-06-11', 200),
('2024-06-12', 200),
('2024-06-12', 300),
('2024-06-13', 300),
('2024-06-13', 200),
('2024-06-14', 200);

INSERT INTO priceadjustment (accommodation_id, price_adjustment_date_id)
VALUES
(1, 1),
(1, 2),
(1, 3),
(1, 4),
(1, 5),
(1, 6),
(1, 7);
INSERT INTO images (accommodation_id,images)
VALUES (1,ARRAY ['https://example.com/image1.jpg', 'https://example.com/image2.jpg']);