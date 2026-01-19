BEGIN;

-- 1. Общие индексы по внешним ключам

-- USERS
CREATE INDEX idx_employee_person_id  ON employee (person_id);
CREATE INDEX idx_client_person_id    ON client (person_id);

-- FILES
CREATE INDEX idx_file_owner_id          ON file (owner_id);
CREATE INDEX idx_file_current_version   ON file (current_version_id);
CREATE INDEX idx_file_version_file_id   ON file_version (file_id);
CREATE INDEX idx_file_version_creator   ON file_version (creator_id);

-- MATERIALS
CREATE INDEX idx_material_current_balance    ON material (current_balance_id);
CREATE INDEX idx_material_balance_material   ON material_balance (material_id);
CREATE INDEX idx_material_balance_prev       ON material_balance (previous_balance_id);
CREATE INDEX idx_material_balance_changer    ON material_balance (changer_id);

-- PURCHASE ORDERS
CREATE INDEX idx_po_status_id        ON purchase_order (current_status_id);
CREATE INDEX idx_po_supply_manager   ON purchase_order (supply_manager_id);
CREATE INDEX idx_pos_po_id           ON purchase_order_status (purchase_order_id);

CREATE INDEX idx_pom_material        ON purchase_order_material (material_id);
CREATE INDEX idx_pom_po              ON purchase_order_material (purchase_order_id);

CREATE INDEX idx_por_po              ON purchase_order_receipt (purchase_order_id);
CREATE INDEX idx_por_worker          ON purchase_order_receipt (warehouse_worker_id);

-- PRODUCT
CREATE INDEX idx_pd_constructor      ON product_design (constructor_id);
CREATE INDEX idx_pdf_pd_id           ON product_design_file (product_design_id);
CREATE INDEX idx_pdf_file_id         ON product_design_file (file_id);
CREATE INDEX idx_rqmt_design         ON required_material (product_design_id);
CREATE INDEX idx_rqmt_material       ON required_material (material_id);

-- CATALOG
CREATE INDEX idx_pc_design_id        ON product_catalog (product_design_id);
CREATE INDEX idx_photo_catalog       ON product_photo (product_catalog_id);

-- CLIENT ORDER
CREATE INDEX idx_clapp_client_id     ON client_application (client_id);
CREATE INDEX idx_clapp_template_pd   ON client_application (template_product_design_id);

CREATE INDEX idx_clappatt_clapp_id   ON client_application_attachment (client_application_id);
CREATE INDEX idx_clappatt_file_id    ON client_application_attachment (file_id);

CREATE INDEX idx_co_status_id        ON client_order (current_status_id);
CREATE INDEX idx_co_client_app_id    ON client_order (client_application_id);
CREATE INDEX idx_co_manager_id       ON client_order (manager_id);
CREATE INDEX idx_co_product_design   ON client_order (product_design_id);

CREATE INDEX idx_cos_order_id        ON client_order_status (client_order_id);
CREATE INDEX idx_cos_status          ON client_order_status (status);

CREATE INDEX idx_mc_order_id         ON material_consumption (client_order_id);
CREATE INDEX idx_mc_material_id      ON material_consumption (material_id);

-- CHAT
CREATE INDEX idx_conv_order_id       ON conversation (order_id);
CREATE INDEX idx_conv_status         ON conversation (status);

CREATE INDEX idx_conv_part_conv_id   ON conversation_participant (conversation_id);
CREATE INDEX idx_conv_part_user_id   ON conversation_participant (user_id);

CREATE INDEX idx_message_part_id     ON message (conversation_participant_id);

-- PRODUCTION
CREATE INDEX idx_pt_order_id         ON production_task (client_order_id);
CREATE INDEX idx_pt_status_id        ON production_task (current_status_id);
CREATE INDEX idx_pt_operator_id      ON production_task (cnc_operator_id);

CREATE INDEX idx_pts_task_id         ON production_task_status (production_task_id);
CREATE INDEX idx_pts_status          ON production_task_status (status);

COMMIT;
