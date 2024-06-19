INSERT INTO public."bookit-accommodation" (name, hostusername, location, filters, minguests, maxguests, pricetype, deleted)
VALUES
    ('Mountain Cabin Retreat', 'username2', 'Aspen', 'wifi,parking,fireplace,bath', 2, 6, 'price_per_person', false),
    ('City Center Studio',  'host2', 'New York', 'wifi,parking,tv', 1, 2, 'price_per_person', false),
    ('Beachfront Condo', 'host2', 'Malibu', 'wifi,parking,pool', 1, 4, 'price_per_unit', false);


INSERT INTO reservation (accommodation_id, guestusername, state, numofguests, fromdate, todate, totalprice, deleted)
VALUES
    (1, 'guestUser1', 'CONFIRMED', 2, '2024-07-10', '2024-07-14', 600.00, false),
    (2, 'guestUser2', 'PENDING', 1, '2024-07-12', '2024-07-16', 400.00, false),
    (2, 'guestUser1', 'APPROVED', 2, '2024-07-21', '2024-07-22', 600.00, false);

INSERT INTO priceadjustmentdate (date, price,reservation_id)
VALUES
    ('2024-07-10', 150,1),
    ('2024-07-11', 150,1),
    ('2024-07-12', 150,1),
    ('2024-07-13', 150,1),
    ('2024-07-14', 150,1),
    ('2024-07-21', 290, 3),
    ('2024-07-22', 290, 3),

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
    ('2024-04-10', 200,null),
    ('2024-04-11', 200,null),
    ('2024-04-12', 200,null),
    ('2024-04-13', 200,null),
    ('2024-04-15', 220,null),
    ('2024-04-16', 220,null),
    ('2024-04-17', 220,null),
    ('2024-04-18', 220,null);

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