ALTER TABLE menu_items ADD COLUMN inventory INTEGER NOT NULL DEFAULT 100;
ALTER TABLE menu_items ADD CONSTRAINT menu_items_inventory_check CHECK (inventory >= 0);
CREATE INDEX idx_menu_items_inventory ON menu_items(inventory);
