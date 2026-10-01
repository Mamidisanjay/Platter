CREATE TABLE deliveries (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL UNIQUE REFERENCES orders(id) ON DELETE CASCADE,
    delivery_partner_id BIGINT NOT NULL REFERENCES users(id),
    status VARCHAR(24) NOT NULL,
    pickup_time TIMESTAMPTZ,
    picked_up_at TIMESTAMPTZ,
    delivered_at TIMESTAMPTZ,
    delivery_address VARCHAR(500) NOT NULL,
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT deliveries_status_check CHECK (status IN ('ASSIGNED','PICKED_UP','OUT_FOR_DELIVERY','DELIVERED','CANCELLED'))
);
CREATE INDEX idx_deliveries_partner_status ON deliveries(delivery_partner_id, status);
CREATE INDEX idx_deliveries_status ON deliveries(status);
