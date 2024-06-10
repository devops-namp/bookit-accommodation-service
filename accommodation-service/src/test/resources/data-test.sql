INSERT INTO bookit-accommodation (name, location, filters, minguests, maxguests, pricetype)
VALUES ('Ocean View Apartment', 'Miami Beach, FL', 'wifi,free parking,kitchen', 1, 4, 'price per unit');

INSERT INTO priceadjustment (fromdate, todate, price, accommodation_id)
VALUES ('2024-06-01', '2024-06-30', 150.00, 1);

INSERT INTO images (accommodation_id,images)
VALUES (1,ARRAY ['https://example.com/image1.jpg', 'https://example.com/image2.jpg']);