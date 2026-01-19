ALTER TABLE production_task_status
    DROP CONSTRAINT chk_production_task_status;

ALTER TABLE production_task_status
    ADD CONSTRAINT chk_production_task_status
        CHECK (status IN (
                          'PENDING',
                          'QUEUED',
                          'IN_PROGRESS',
                          'COMPLETED'
            ));
