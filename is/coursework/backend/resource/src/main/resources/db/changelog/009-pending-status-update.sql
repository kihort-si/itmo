ALTER TABLE client_order_status
    DROP CONSTRAINT IF EXISTS chk_client_order_status;

ALTER TABLE client_order_status
    ADD CONSTRAINT chk_client_order_status
        CHECK (status IN (
                          'CREATED',
                          'IN_PROGRESS',
                          'CONSTRUCTOR_PENDING_APPROVAL',
                          'CLIENT_PENDING_APPROVAL',
                          'REWORK',
                          'READY_FOR_PRODUCTION',
                          'IN_PRODUCTION',
                          'READY_FOR_PICKUP',
                          'COMPLETED'
            ));