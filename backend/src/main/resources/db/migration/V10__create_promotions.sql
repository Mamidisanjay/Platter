CREATE TABLE promo_codes (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(80) NOT NULL UNIQUE,
    discount_type VARCHAR(16) NOT NULL,
    discount_value NUMERIC(12,2) NOT NULL,
    minimum_order_amount NUMERIC(12,2) NOT NULL DEFAULT 0,
    maximum_discount NUMERIC(12,2),
    start_time TIMESTAMPTZ NOT NULL,
    expiry_time TIMESTAMPTZ NOT NULL,
    usage_limit INTEGER NOT NULL DEFAULT 0,
    usage_count INTEGER NOT NULL DEFAULT 0,
    per_user_limit INTEGER NOT NULL DEFAULT 1,
    restaurant_id BIGINT REFERENCES restaurants(id),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT promo_discount_type_check CHECK (discount_type IN ('PERCENTAGE','FIXED'))
);
CREATE INDEX idx_promo_codes_active_expiry ON promo_codes(active, expiry_time);
CREATE TABLE promo_usages (
    id BIGSERIAL PRIMARY KEY,
    promo_id BIGINT NOT NULL REFERENCES promo_codes(id),
    user_id BIGINT NOT NULL REFERENCES users(id),
    order_id BIGINT REFERENCES orders(id),
    used_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_promo_usage_user_promo ON promo_usages(user_id, promo_id);
