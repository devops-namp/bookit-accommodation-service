INSERT INTO public."bookit-accommodation" (autoAcceptReservations, name, hostusername, location, filters, minguests, maxguests, pricetype, deleted)
VALUES
(false, 'Mountain Cabin Retreat', 'username2', 'Aspen', 'wifi,parking,fireplace,bath', 2, 6, 'price_per_person', false),
(false, 'City Center Studio',  'host2', 'New York', 'wifi,parking,tv', 1, 2, 'price_per_person', false),
(false, 'Beachfront Condo', 'host2', 'Malibu', 'wifi,parking,pool', 1, 4, 'price_per_unit', false),
(false, 'Beachfront Condo123', 'username3', 'Malibu', 'wifi,parking,pool', 1, 4, 'price_per_person', false);


INSERT INTO reservation (accommodation_id, guestusername, state, numofguests, fromdate, todate, totalprice, deleted)
VALUES
(1, 'guestUser1', 'APPROVED', 2, '2024-07-10', '2024-07-14', 600.00, false),
(1, 'guestUser1', 'APPROVED', 2, '2024-03-10', '2024-03-14', 600.00, false),
(2, 'guestUser2', 'PENDING', 1, '2024-07-12', '2024-07-16', 400.00, false),
(4, 'guestUser2', 'APPROVED', 1, '2024-01-12', '2024-01-16', 500.00, false);

INSERT INTO priceadjustmentdate (date, price,reservation_id)
VALUES
('2024-07-10', 150,1),
('2024-07-11', 150,1),
('2024-07-12', 150,1),
('2024-07-13', 150,1),
('2024-07-14', 150,1),

('2024-07-10', 100,null),
('2024-07-11', 100,null),
('2024-07-12', 100,null),
('2024-07-13', 100,null),
('2024-07-14', 100,null),

('2024-07-12', 250,null),
('2024-07-13', 250,null),
('2024-07-14', 250,null),
('2024-07-15', 250,null),
('2024-07-16', 250,null),

('2024-07-10', 100,3),
('2024-07-11', 150,3),
('2024-07-12', 250,3),
('2024-07-13', 250,3),
('2024-07-14', 150,3);

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
(3, 15),
(4, 16),
(4, 17),
(4, 18),
(4, 19),
(4, 20);