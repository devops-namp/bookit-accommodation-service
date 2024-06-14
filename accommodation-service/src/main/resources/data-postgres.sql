INSERT INTO public."bookit-accommodation" (name, hostusername, location, filters, minguests, maxguests, pricetype)
VALUES
('Mountain Cabin Retreat', 'username2', 'Aspen, CO', 'wifi,parking,fireplace,bath', 2, 6, 'price_per_person'),
('City Center Studio',  'host2', 'New York, NY', 'wifi,parking,tv', 1, 2, 'price_per_person'),
('Beachfront Condo', 'host2', 'Malibu, CA', 'wifi,parking,pool', 1, 4, 'price_per_unit');

INSERT INTO priceadjustmentdate (date, price)
VALUES
('2024-07-10', 150),
('2024-07-11', 150),
('2024-07-12', 150),
('2024-07-13', 150),
('2024-07-14', 150),

('2024-07-10', 100),
('2024-07-11', 100),
('2024-07-12', 100),
('2024-07-13', 100),
('2024-07-14', 100),

('2024-07-12', 250),
('2024-07-13', 250),
('2024-07-14', 250),
('2024-07-15', 250),
('2024-07-16', 250);

INSERT INTO priceadjustment (accommodation_id, price_adjustment_date_id)
VALUES
(1, 1),
(1, 2),
(1, 3),
(1, 4),
(1, 5),
(2, 6),
(2, 7),
(2, 8),
(2, 9),
(2, 10),
(3, 11),
(3, 12),
(3, 13),
(3, 14),
(3, 15);

-- INSERT INTO images (accommodation_id, image_data)
-- VALUES
-- (1, decode('9d1d1414792cb45cdbaf2035461e7e8d2c81cce9edc7d5ee1d5a149859779c51', 'hex')),
-- (2, decode('f0ea024f8ca6635a3705626675fce84d9f15e0b4d24e4cfc2b1670557c8c6a54', 'hex')),
-- (3, decode('a30fb149bc14d56db364879b8a9ca4ad134cffbc85a7e95a8557b0ca3177119b', 'hex'));

INSERT INTO reservation (accommodation_id, price_adjustment_date_id)
VALUES
(1, 2),
(1, 3);