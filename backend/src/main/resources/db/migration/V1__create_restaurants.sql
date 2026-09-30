CREATE TABLE restaurants (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    cuisine VARCHAR(255) NOT NULL,
    rating NUMERIC(2,1) NOT NULL DEFAULT 0,
    delivery_time_minutes INTEGER NOT NULL,
    price_tier VARCHAR(10) NOT NULL,
    image_url VARCHAR(500),
    tag VARCHAR(80),
    accent VARCHAR(20),
    available BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_restaurants_available ON restaurants(available);
CREATE INDEX idx_restaurants_name ON restaurants(name);
INSERT INTO restaurants (name, cuisine, rating, delivery_time_minutes, price_tier, image_url, tag, accent) VALUES
('Saffron Street', 'North Indian, Biryani', 4.8, 28, '$$', 'https://images.unsplash.com/photo-1585937421612-70a008356fbe?auto=format&fit=crop&w=900&q=85', 'Best seller', '#ef6c45'),
('Tokyo Table', 'Japanese, Ramen', 4.7, 34, '$$$', 'https://images.unsplash.com/photo-1569718212165-3a8278d5f624?auto=format&fit=crop&w=900&q=85', 'Top rated', '#5878c9'),
('The Green Room', 'Healthy, Salads', 4.6, 22, '$$', 'https://images.unsplash.com/photo-1512621776951-a57141f2eefd?auto=format&fit=crop&w=900&q=85', 'Fresh pick', '#6d9c70'),
('Ember & Grain', 'American, Grill', 4.5, 41, '$$$', 'https://images.unsplash.com/photo-1550547660-d9450f859349?auto=format&fit=crop&w=900&q=85', 'New on Platter', '#b88754');
