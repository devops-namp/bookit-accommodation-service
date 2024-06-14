INSERT INTO public."bookit-accommodation" (name, hostusername, location, filters, minguests, maxguests, pricetype)
VALUES ('Ocean View Apartment', 'host', 'Miami Beach, FL', 'wifi,parking,kitchen', 1, 4, 'price_per_unit');

INSERT INTO priceadjustmentdate (date, price)
VALUES ('2024-06-10', 200);

INSERT INTO priceadjustment (accommodation_id, price_adjustment_date_id)
VALUES (1, 1);

-- INSERT INTO images (accommodation_id,images)
-- VALUES (1,ARRAY ['https://example.com/image1.jpg', 'https://example.com/image2.jpg']);