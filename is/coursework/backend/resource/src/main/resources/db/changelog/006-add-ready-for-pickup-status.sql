-- Добавление нового статуса READY_FOR_PICKUP в CHECK constraint для client_order_status
ALTER TABLE client_order_status
DROP CONSTRAINT IF EXISTS chk_client_order_status;

ALTER TABLE client_order_status
ADD CONSTRAINT chk_client_order_status
    CHECK (status IN (
                      'CREATED',
                      'IN_PROGRESS',
                      'PENDING_APPROVAL',
                      'REWORK',
                      'APPROVED',
                      'READY_FOR_PRODUCTION',
                      'IN_PRODUCTION',
                      'READY_FOR_PICKUP',
                      'COMPLETED'
        ));

ALTER TABLE purchase_order_material
ADD real_amount NUMERIC(12, 2);