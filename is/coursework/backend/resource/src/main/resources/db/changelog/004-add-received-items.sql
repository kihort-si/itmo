-- Добавление колонки received_items в таблицу purchase_order_receipt
ALTER TABLE purchase_order_receipt
ADD COLUMN received_items JSONB;

