INSERT INTO public."bookit-accommodation" (name, location, filters, minguests, maxguests, pricetype)
VALUES
('Mountain Cabin Retreat', 'Aspen, CO', 'wifi,free parking,kitchen,fireplace', 2, 6, 'price per person'),
('City Center Studio', 'New York, NY', 'wifi,free parking,air conditioning', 1, 2, 'price per person'),
('Beachfront Condo', 'Malibu, CA', 'wifi,free parking,pool', 1, 4, 'price per unit');

INSERT INTO priceadjustmentdate (date, price)
VALUES
('2024-06-10', 150),
('2024-06-11', 150),
('2024-06-12', 150),
('2024-06-13', 150),
('2024-06-14', 150),
('2024-06-10', 100),
('2024-06-11', 100),
('2024-06-12', 100),
('2024-06-13', 100),
('2024-06-14', 100),
('2024-06-10', 250),
('2024-06-11', 250),
('2024-06-12', 250),
('2024-06-13', 250);

INSERT INTO priceadjustment (accommodation_id, price_adjustment_date_id)
VALUES
(1, 1),
(1, 2),
(1, 3),
(1, 4),
(2, 5),
(2, 6),
(2, 7),
(2, 8),
(2, 9),
(3, 10),
(3, 11),
(3, 12),
(3, 13),
(3, 14);

INSERT INTO images (accommodation_id, images)
VALUES
(1, ARRAY ['https://example.com/image3.jpg', 'https://example.com/image4.jpg']),
(2, ARRAY ['https://example.com/image5.jpg', 'https://example.com/image6.jpg']),
(3, ARRAY ['https://example.com/image7.jpg', 'https://example.com/image8.jpg']);

-- INSERT INTO reservation (accommodation_id, price_adjustment_date_id)
-- VALUES
-- (1, 3),
-- (2, 6),
-- (3, 13);