CREATE INDEX idx_message_sender    ON message(sender_id, created_at DESC);
CREATE INDEX idx_message_receiver  ON message(receiver_id, created_at DESC);
-- Обоснование: входящие/исходящие сортируются по времени.

CREATE INDEX idx_file_owner              ON file(owner_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_file_version_file_time  ON file_version(file_id, uploaded_at DESC);
-- Обоснование: список файлов владельца; история версий, быстрый доступ к последним.

CREATE INDEX idx_product_catalog_design  ON product_catalog(product_design_id);
CREATE INDEX idx_product_photo_file      ON product_photo(file_id);
CREATE INDEX idx_product_photo_catalog   ON product_photo(product_catalog_id);
CREATE INDEX idx_pdf_design              ON product_design_file(product_design_id);
CREATE INDEX idx_pdf_file                ON product_design_file(file_id);
-- Обоснование: карточка товара, подбор медиа и чертежей.

CREATE INDEX idx_material_current_balance  ON material(current_balance_id);
CREATE INDEX idx_mb_material_time          ON material_balance(material_id, changed_at DESC);
CREATE INDEX idx_required_material_design  ON required_material(product_design_id);
-- Обоснование: быстрый доступ к текущему остатку, истории и нормам списания.

CREATE INDEX idx_clapp_client            ON client_application(client_id);
CREATE INDEX idx_clapp_template_design   ON client_application(template_product_design_id);
CREATE INDEX idx_co_client_app           ON client_order(client_application_id);
CREATE INDEX idx_co_manager              ON client_order(manager_id);
CREATE INDEX idx_co_product_design       ON client_order(product_design_id);
CREATE INDEX idx_cos_order_time          ON client_order_status(client_order_id, set_at DESC);
CREATE INDEX idx_mc_order                ON material_consumption(order_id);
CREATE INDEX idx_mc_material             ON material_consumption(material_id);
-- Обоснование: экраны «мои заявки/заказы», смена статусов, списания по заказу и материалу.

CREATE INDEX idx_pt_design               ON production_task(product_design_id);
CREATE INDEX idx_pts_task_time           ON production_task_status(production_task_id, set_at DESC);
CREATE INDEX idx_issue_task_time         ON production_issue(production_task_id, occurred_at DESC);
-- Обоснование: один таск на дизайн, быстрый последний статус, журнал инцидентов.

CREATE INDEX idx_po_manager              ON purchase_order(supply_manager_id);
CREATE INDEX idx_pos_po_time             ON purchase_order_status(purchase_order_id, set_at DESC);
CREATE INDEX idx_pom_po                  ON purchase_order_material(purchase_order_id);
CREATE INDEX idx_pom_material            ON purchase_order_material(material_id);
CREATE INDEX idx_por_worker              ON purchase_order_receipt(warehouse_worker_id);
-- Обоснование: листинг закупок по менеджеру, последний статус, состав и приёмка.

CREATE INDEX idx_email_token_client      ON email_token(client_id);
CREATE INDEX idx_email_token_exp         ON email_token(expiration_dt);
-- Обоснование: проверка токена клиента и периодическая очистка просроченных.
