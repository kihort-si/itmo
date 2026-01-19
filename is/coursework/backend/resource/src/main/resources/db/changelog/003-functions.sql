BEGIN;

-- Сводка по заказу
CREATE OR REPLACE VIEW v_client_order_summary AS
SELECT
    co.id                AS order_id,
    co.created_at        AS order_created_at,
    co.price,
    ca.amount,
    cos.status           AS current_status,
    c.email              AS client_email,
    pc.name              AS product_name,
    a_manager.username   AS manager_username
FROM client_order co
         JOIN client_order_status cos ON cos.id = co.current_status_id
         JOIN client_application ca   ON ca.id = co.client_application_id
         JOIN client c                ON c.id = ca.client_id
         JOIN product_design pd       ON pd.id = co.product_design_id
         LEFT JOIN product_catalog pc ON pc.product_design_id = pd.id
         JOIN employee e_manager      ON e_manager.id = co.manager_id
         JOIN account a_manager       ON a_manager.id = e_manager.account_id;

-- 2. Требуемые материалы по заказу
CREATE OR REPLACE FUNCTION f_order_required_materials(p_order_id BIGINT)
    RETURNS TABLE (
                      material_id BIGINT,
                      material_name VARCHAR,
                      total_required NUMERIC
                  )
    LANGUAGE sql
    STABLE
AS $$
SELECT
    m.id,
    m.name,
    rm.amount * ca.amount AS total_required
FROM client_order co
         JOIN client_application ca ON ca.id = co.client_application_id
         JOIN product_design pd     ON pd.id = co.product_design_id
         JOIN required_material rm  ON rm.product_design_id = pd.id
         JOIN material m            ON m.id = rm.material_id
WHERE co.id = p_order_id;
$$;

-- Дефицит материалов по заказу (shortage)
CREATE OR REPLACE FUNCTION f_order_material_shortage(p_order_id BIGINT)
    RETURNS TABLE (
                      material_id BIGINT,
                      material_name VARCHAR,
                      required NUMERIC,
                      available NUMERIC,
                      shortage NUMERIC
                  )
    LANGUAGE sql
    STABLE
AS $$
WITH req AS (
    SELECT * FROM f_order_required_materials(p_order_id)
)
SELECT
    r.material_id,
    r.material_name,
    r.total_required AS required,
    mb.balance       AS available,
    GREATEST(r.total_required - mb.balance, 0) AS shortage
FROM req r
         JOIN material m ON m.id = r.material_id
         JOIN material_balance mb ON mb.id = m.current_balance_id;
$$;

-- История статусов заказа
CREATE OR REPLACE FUNCTION f_client_order_status_history(p_order_id BIGINT)
    RETURNS TABLE (
                      status VARCHAR(50),
                      set_at TIMESTAMPTZ
                  )
    LANGUAGE sql
    STABLE
AS $$
SELECT status, set_at
FROM client_order_status
WHERE client_order_id = p_order_id
ORDER BY set_at ASC, id ASC;
$$;

-- История статусов производственной задачи
CREATE OR REPLACE FUNCTION f_production_task_status_history(p_task_id BIGINT)
    RETURNS TABLE (
                      status VARCHAR(50),
                      set_at TIMESTAMPTZ
                  )
    LANGUAGE sql
    STABLE
AS $$
SELECT status, set_at
FROM production_task_status
WHERE production_task_id = p_task_id
ORDER BY set_at ASC, id ASC;
$$;

-- Последнее сообщение по заказу
CREATE OR REPLACE FUNCTION f_order_last_message(p_order_id BIGINT)
    RETURNS TABLE (
                      message_id BIGINT,
                      content TEXT,
                      sent_at TIMESTAMPTZ,
                      username VARCHAR(50)
                  )
    LANGUAGE sql
    STABLE
AS $$
SELECT m.id,
       m.content,
       m.sent_at,
       a.username
FROM conversation conv
         JOIN conversation_participant cp ON cp.conversation_id = conv.id
         JOIN message m ON m.conversation_participant_id = cp.id
         JOIN account a ON a.id = cp.user_id
WHERE conv.order_id = p_order_id
ORDER BY m.sent_at DESC, m.id DESC
LIMIT 1;
$$;

-- Участники диалогов
CREATE OR REPLACE VIEW v_conversation_participants AS
SELECT
    conv.id          AS conversation_id,
    conv.order_id    AS order_id,
    a.id             AS account_id,
    a.username,
    a.role,
    cp.joined_at
FROM conversation conv
         JOIN conversation_participant cp ON cp.conversation_id = conv.id
         JOIN account a ON a.id = cp.user_id;

-- Текущие остатки материалов
CREATE OR REPLACE VIEW v_material_stock AS
SELECT
    m.id,
    m.name,
    m.unit_of_measure,
    mb.balance,
    m.order_point,
    (mb.balance <= m.order_point) AS need_reorder
FROM material m
         LEFT JOIN material_balance mb ON mb.id = m.current_balance_id;

-- Функции для обновления текущего статуса/баланса/версии
-- 1) client_order: добавить статус и сделать его текущим
CREATE OR REPLACE PROCEDURE p_update_client_order_status_and_set_current(
    p_client_order_id BIGINT,
    p_status VARCHAR
)
    LANGUAGE plpgsql
AS $$
DECLARE
    v_new_status_id BIGINT;
BEGIN
    INSERT INTO client_order_status (client_order_id, status)
    VALUES (p_client_order_id, p_status)
    RETURNING id INTO v_new_status_id;

    UPDATE client_order
    SET current_status_id = v_new_status_id
    WHERE id = p_client_order_id;
END;
$$;


-- 2) production_task: добавить статус и сделать его текущим
CREATE OR REPLACE PROCEDURE p_update_production_task_status_and_set_current(
    p_task_id BIGINT,
    p_status VARCHAR
)
    LANGUAGE plpgsql
AS $$
DECLARE
    v_new_status_id BIGINT;
BEGIN
    INSERT INTO production_task_status (production_task_id, status)
    VALUES (p_task_id, p_status)
    RETURNING id INTO v_new_status_id;

    UPDATE production_task
    SET current_status_id = v_new_status_id
    WHERE id = p_task_id;
END;
$$;


-- 3) purchase_order: добавить статус и сделать его текущим
CREATE OR REPLACE PROCEDURE p_update_purchase_order_status_and_set_current(
    p_po_id BIGINT,
    p_status VARCHAR
)
    LANGUAGE plpgsql
AS $$
DECLARE
    v_new_status_id BIGINT;
BEGIN
    INSERT INTO purchase_order_status (purchase_order_id, status)
    VALUES (p_po_id, p_status)
    RETURNING id INTO v_new_status_id;

    UPDATE purchase_order
    SET current_status_id = v_new_status_id
    WHERE id = p_po_id;
END;
$$;


-- 4) material: добавить баланс и сделать его текущим
CREATE OR REPLACE PROCEDURE p_update_material_balance_and_set_current(
    p_material_id BIGINT,
    p_new_balance NUMERIC,
    p_changer_id BIGINT
)
    LANGUAGE plpgsql
AS $$
DECLARE
    v_current_balance_id BIGINT;
    v_new_balance_id BIGINT;
BEGIN
    SELECT current_balance_id
    INTO v_current_balance_id
    FROM material
    WHERE id = p_material_id;

    INSERT INTO material_balance (material_id, balance, previous_balance_id, changer_id)
    VALUES (p_material_id, p_new_balance, v_current_balance_id, p_changer_id)
    RETURNING id INTO v_new_balance_id;

    UPDATE material
    SET current_balance_id = v_new_balance_id
    WHERE id = p_material_id;
END;
$$;


-- 5) file: добавить версию файла и сделать её текущей
CREATE OR REPLACE PROCEDURE p_update_file_version_and_set_current(
    p_file_id BIGINT,
    p_bucket TEXT,
    p_object_key TEXT,
    p_size_bytes BIGINT,
    p_content_type VARCHAR,
    p_creator_id BIGINT
)
    LANGUAGE plpgsql
AS $$
DECLARE
    v_new_version_id BIGINT;
BEGIN
    INSERT INTO file_version (creator_id, bucket, object_key, size_bytes, content_type, file_id)
    VALUES (p_creator_id, p_bucket, p_object_key, p_size_bytes, p_content_type, p_file_id)
    RETURNING id INTO v_new_version_id;

    UPDATE file
    SET current_version_id = v_new_version_id
    WHERE id = p_file_id;
END;
$$;

COMMIT;