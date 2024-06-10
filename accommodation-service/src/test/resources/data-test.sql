INSERT INTO public."bookit-accommodation" (name, location, filters, minguests, maxguests, pricetype)
VALUES ('Ocean View Apartment', 'Miami Beach, FL', 'wifi,free parking,kitchen', 1, 4, 'price per unit');

INSERT INTO priceadjustmentdate (date, price)
VALUES ('2024-06-10', 200);

INSERT INTO priceadjustment (accommodation_id, price_adjustment_date_id)
VALUES (1, 1);

INSERT INTO images (accommodation_id,images)
VALUES (1,ARRAY ['https://example.com/image1.jpg', 'https://example.com/image2.jpg']);