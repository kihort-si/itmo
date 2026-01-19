BEGIN;

-- На всякий случай: проверим, что отложимые связи действительно откладываются
SET CONSTRAINTS ALL DEFERRED;

-------------------------
-- 1) Базовые учётки/люди
-------------------------
WITH acc AS (
    INSERT INTO account (username, password, enabled)
        VALUES ('admin', '{bcrypt}$2a$10$demo_admin_hash', TRUE),
               ('alice', '{bcrypt}$2a$10$demo_alice_hash', TRUE),
               ('bob', '{bcrypt}$2a$10$demo_bob_hash', TRUE),
               ('carol', '{bcrypt}$2a$10$demo_carol_hash', TRUE),
               ('dave', '{bcrypt}$2a$10$demo_dave_hash', TRUE)
        RETURNING id, username),
     prs AS (
         INSERT INTO person (first_name, last_name) VALUES ('Alice', 'Mayer'),
                                                           ('Bob', 'Novak'),
                                                           ('Carol', 'Kim'),
                                                           ('Dave', 'Stone'),
                                                           ('Eve', 'Fox')
             RETURNING id, first_name)
INSERT
INTO employee (account_id, person_id)
SELECT a.id, p.id
FROM acc a
         JOIN prs p ON (a.username, p.first_name) IN (('admin', 'Eve'),
                                                      ('carol', 'Carol'),
                                                      ('dave', 'Dave'));
-- Итого: сотрудники = admin(Eve), carol(Carol), dave(Dave); alice/bob — клиенты.

-- 2) Клиенты и токены
WITH a AS (SELECT id FROM account WHERE username IN ('alice', 'bob') ORDER BY username),
     p AS (SELECT id, first_name FROM person WHERE first_name IN ('Alice', 'Bob'))
INSERT
INTO client (email, person_id, account_id, phone_number, address)
VALUES ('alice@example.com',
        (SELECT id FROM p WHERE first_name = 'Alice'),
        (SELECT id FROM a LIMIT 1 OFFSET 0),
        '+44 20 7946 0011',
        '221B Baker Street, London'),
       ('bob@example.com',
        (SELECT id FROM p WHERE first_name = 'Bob'),
        (SELECT id FROM a LIMIT 1 OFFSET 1),
        '+44 20 7946 0022',
        '10 Downing St, London');

INSERT INTO email_token (token, client_id, expiration_dt)
SELECT 'verify-alice', (SELECT id FROM client WHERE email = 'alice@example.com'), now() + interval '7 days'
UNION ALL
SELECT 'verify-bob', (SELECT id FROM client WHERE email = 'bob@example.com'), now() + interval '7 days';

-- 3) Переписка
INSERT INTO message (content, sender_id, receiver_id)
SELECT 'Hi Bob!', a1.id, a2.id
FROM account a1,
     account a2
WHERE a1.username = 'alice'
  AND a2.username = 'bob'
UNION ALL
SELECT 'Hi Alice, got it.', a2.id, a1.id
FROM account a1,
     account a2
WHERE a1.username = 'alice'
  AND a2.username = 'bob';

-- 4) Конструкторские дизайны и каталог
WITH emp AS (SELECT (SELECT e.id
                     FROM employee e
                              JOIN account aa ON e.account_id = aa.id
                     WHERE aa.username = 'carol'
                     LIMIT 1) AS constructor_id),
     i AS (
         INSERT INTO product_design (constructor_id, product_name)
             VALUES ((SELECT constructor_id FROM emp), 'Alu Panel A'),
                    ((SELECT constructor_id FROM emp), 'Steel Bracket B'),
                    (NULL, 'Prototype C')
             RETURNING id, product_name)
INSERT
INTO product_catalog (name, description, product_design_id, price, minimal_amount)
SELECT 'Alu Panel A / v1',
       'Aluminium front panel',
       (SELECT id FROM i WHERE product_name = 'Alu Panel A'),
       149.90,
       5
UNION ALL
SELECT 'Steel Bracket B / v1',
       'Heavy duty bracket',
       (SELECT id FROM i WHERE product_name = 'Steel Bracket B'),
       89.50,
       10;

-- 5) Файлы, версии и фото
WITH seqs AS (SELECT nextval('file_version_id_seq') AS v1,
                     nextval('file_version_id_seq') AS v2,
                     nextval('file_version_id_seq') AS v3),
     owners AS (SELECT (SELECT id FROM account WHERE username = 'admin') AS owner_id,
                       (SELECT id FROM account WHERE username = 'carol') AS carol_id),
     files AS (
         INSERT INTO file (filename, content_type, current_version_id, owner_id)
             SELECT 'design-a.step', 'application/STEP', s.v1, o.owner_id
             FROM seqs s,
                  owners o
             UNION ALL
             SELECT 'drawing-b.pdf', 'application/pdf', s.v2, o.carol_id
             FROM seqs s,
                  owners o
             UNION ALL
             SELECT 'photo-a.jpg', 'image/jpeg', s.v3, o.owner_id
             FROM seqs s,
                  owners o
             RETURNING id, filename, content_type, current_version_id, owner_id),
     fv AS (
         INSERT INTO file_version (id, creator_id, bucket, object_key, size_bytes, content_type, file_id)
             OVERRIDING SYSTEM VALUE
             SELECT current_version_id, owner_id, 'demo-bucket', 'files/' || filename, 1024, content_type, id
             FROM files
             RETURNING id, file_id)
INSERT
INTO product_photo (file_id, product_catalog_id)
SELECT (SELECT id FROM files WHERE filename = 'photo-a.jpg'),
       (SELECT id FROM product_catalog WHERE name LIKE 'Alu Panel A%');

-- 6) Материалы и остатки
INSERT INTO required_material (material_id, product_design_id, amount)
SELECT m.id, pd.id, 1.5
FROM material m
         JOIN product_design pd ON pd.product_name = 'Alu Panel A'
WHERE m.name = 'Aluminium Sheet 2mm'
UNION ALL
SELECT m.id, pd.id, 2.0
FROM material m
         JOIN product_design pd ON pd.product_name = 'Steel Bracket B'
WHERE m.name = 'Steel Bar Ø10';

-- 7) Заявки клиентов и вложения
WITH ca AS (
    INSERT INTO client_application (client_id, description, template_product_design_id, amount)
        VALUES ((SELECT id FROM client WHERE email = 'alice@example.com'), 'Need 8 panels, anodized',
                (SELECT id FROM product_design WHERE product_name = 'Alu Panel A'), 8),
               ((SELECT id FROM client WHERE email = 'bob@example.com'), '20 steel brackets',
                (SELECT id FROM product_design WHERE product_name = 'Steel Bracket B'), 20)
        RETURNING id, client_id)
INSERT
INTO client_application_attachment (client_application_id, file_id)
SELECT (SELECT id FROM ca WHERE client_id = (SELECT id FROM client WHERE email = 'alice@example.com')),
       (SELECT id FROM file WHERE filename = 'drawing-b.pdf');

-- 8) Заказы + статусы + списания
WITH seq AS (SELECT nextval('client_order_status_id_seq') AS st1,
                    nextval('client_order_status_id_seq') AS st2),
     mgr AS (SELECT e.id AS manager_id
             FROM employee e
                      JOIN account a ON a.id = e.account_id
             WHERE a.username = 'carol'
             LIMIT 1),
     ord AS (
         INSERT INTO client_order (current_status_id, client_application_id, manager_id, product_design_id, price)
             -- заказ 1: 8 панелей
             SELECT s.st1,
                    ca1.id,
                    (SELECT manager_id FROM mgr),
                    pd1.id,
                    8 * 149.90
             FROM seq s
                      JOIN client_application ca1 ON ca1.description LIKE 'Need 8%'
                      JOIN product_design pd1 ON pd1.product_name = 'Alu Panel A'
             UNION ALL
             -- заказ 2: 20 кронштейнов
             SELECT s.st2,
                    ca2.id,
                    (SELECT manager_id FROM mgr),
                    pd2.id,
                    20 * 89.50
             FROM seq s
                      JOIN client_application ca2 ON ca2.description LIKE '20 steel%'
                      JOIN product_design pd2 ON pd2.product_name = 'Steel Bracket B'
             RETURNING id, current_status_id),
     st AS (
         INSERT INTO client_order_status (id, client_order_id, status)
             OVERRIDING SYSTEM VALUE
             SELECT o.current_status_id, o.id, 'CREATED'
             FROM ord o
             RETURNING id)
-- списание материалов под оба заказа
INSERT
INTO material_consumption (order_id, material_id, amount)
SELECT o1.id, m1.id, 8 * 1.5
FROM client_order o1
         JOIN client_application ca1 ON ca1.id = o1.client_application_id AND ca1.description LIKE 'Need 8%'
         JOIN material m1 ON m1.name = 'Aluminium Sheet 2mm'
UNION ALL
SELECT o2.id, m2.id, 20 * 2.0
FROM client_order o2
         JOIN client_application ca2 ON ca2.id = o2.client_application_id AND ca2.description LIKE '20 steel%'
         JOIN material m2 ON m2.name = 'Steel Bar Ø10';

-- 9) Производственная задача + статус + инцидент
-- 1) создаём задачу без статуса
WITH t AS (
    INSERT INTO production_task (product_design_id, started_at, finished_at, cnc_operator_id)
        SELECT pd.id,
               now() - interval '2 hours',
               now() + interval '1 hours',
               e.id
        FROM product_design pd
                 JOIN employee e ON e.account_id = (SELECT id FROM account WHERE username = 'dave' LIMIT 1)
        WHERE pd.product_name = 'Alu Panel A'
        RETURNING id),
-- 2) создаём статус
     s AS (
         INSERT INTO production_task_status (production_task_id, status)
             SELECT id, 'IN_PROGRESS' FROM t
             RETURNING id AS status_id, production_task_id)

-- 3) инцидент к этой задаче
INSERT
INTO production_issue (production_task_id, severity, description)
VALUES (1, 'minor', 'Coolant level low');

-- 10) Закупка: заказ + статус + состав
WITH mgr AS (SELECT e.id AS sup_mgr
             FROM employee e
                      JOIN account a ON a.id = e.account_id
             WHERE a.username = 'carol'
             LIMIT 1),
     po AS ( -- 1) создаём заказ
         INSERT INTO purchase_order (supply_manager_id)
             SELECT sup_mgr FROM mgr
             RETURNING id),
     st AS ( -- 2) создаём статус
         INSERT INTO purchase_order_status (purchase_order_id, status)
             SELECT id, 'CREATED' FROM po
             RETURNING id AS status_id, purchase_order_id)

-- 3) состав заказа
INSERT
INTO purchase_order_material (material_id, purchase_order_id, amount, price_for_unit, supplier)
SELECT m.id, (SELECT purchase_order_id FROM st), 300, 12.5, 'AluSupply Ltd'
FROM material m
WHERE m.name = 'Aluminium Sheet 2mm'
UNION ALL
SELECT m.id, (SELECT purchase_order_id FROM st), 400, 3.2, 'SteelCorp PLC'
FROM material m
WHERE m.name = 'Steel Bar Ø10';


-- 11) Обновим остатки материалов после закупки
-- Aluminium +300 -> 500
WITH mb_prev AS (SELECT m.id AS material_id, m.current_balance_id AS prev_id
                 FROM material m
                 WHERE m.name = 'Aluminium Sheet 2mm'),
     adm AS (SELECT id AS changer FROM account WHERE username = 'admin'),
     mb_new AS (
         INSERT INTO material_balance (material_id, balance, previous_balance_id, changer_id)
             SELECT material_id, 500, prev_id, (SELECT changer FROM adm) FROM mb_prev
             RETURNING id)
UPDATE material
SET current_balance_id = (SELECT id FROM mb_new)
WHERE id = (SELECT material_id FROM mb_prev);

-- Steel +400 -> 900
WITH mb_prev AS (SELECT m.id AS material_id, m.current_balance_id AS prev_id
                 FROM material m
                 WHERE m.name = 'Steel Bar Ø10'),
     adm AS (SELECT id AS changer FROM account WHERE username = 'admin'),
     mb_new AS (
         INSERT INTO material_balance (material_id, balance, previous_balance_id, changer_id)
             SELECT material_id, 900, prev_id, (SELECT changer FROM adm) FROM mb_prev
             RETURNING id)
UPDATE material
SET current_balance_id = (SELECT id FROM mb_new)
WHERE id = (SELECT material_id FROM mb_prev);

COMMIT;