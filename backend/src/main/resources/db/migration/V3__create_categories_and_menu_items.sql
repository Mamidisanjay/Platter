CREATE TABLE categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE menu_items (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(160) NOT NULL,
    description VARCHAR(500),
    price NUMERIC(12,2) NOT NULL CHECK (price >= 0),
    image_url VARCHAR(500),
    available BOOLEAN NOT NULL DEFAULT TRUE,
    restaurant_id BIGINT NOT NULL REFERENCES restaurants(id) ON DELETE CASCADE,
    category_id BIGINT NOT NULL REFERENCES categories(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_menu_items_restaurant_available ON menu_items(restaurant_id, available);
CREATE INDEX idx_menu_items_category ON menu_items(category_id);

INSERT INTO categories (name) VALUES ('Mains'), ('Ramen'), ('Salads'), ('Burgers');
INSERT INTO menu_items (name, description, price, image_url, restaurant_id, category_id)
SELECT 'Butter chicken bowl', 'Creamy tomato curry with basmati rice.', 18.50, NULL, r.id, c.id FROM restaurants r, categories c WHERE r.name = 'Saffron Street' AND c.name = 'Mains';
INSERT INTO menu_items (name, description, price, image_url, restaurant_id, category_id)
SELECT 'Tonkotsu ramen', 'Slow-cooked pork broth, noodles, egg, and greens.', 16.00, NULL, r.id, c.id FROM restaurants r, categories c WHERE r.name = 'Tokyo Table' AND c.name = 'Ramen';
INSERT INTO menu_items (name, description, price, image_url, restaurant_id, category_id)
SELECT 'Green goddess salad', 'Seasonal greens, avocado, herbs, and seeds.', 14.00, NULL, r.id, c.id FROM restaurants r, categories c WHERE r.name = 'The Green Room' AND c.name = 'Salads';
INSERT INTO menu_items (name, description, price, image_url, restaurant_id, category_id)
SELECT 'Ember cheeseburger', 'Charred beef, aged cheddar, and house pickles.', 16.00, NULL, r.id, c.id FROM restaurants r, categories c WHERE r.name = 'Ember & Grain' AND c.name = 'Burgers';
