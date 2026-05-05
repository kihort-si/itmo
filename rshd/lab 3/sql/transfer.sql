\if :{?from_id}
\else
\set from_id 1
\endif

\if :{?to_id}
\else
\set to_id 2
\endif

\if :{?amount}
\else
\set amount 150.00
\endif

\if :{?note}
\else
\set note demo-transfer
\endif

BEGIN;
UPDATE accounts
SET balance = balance - :amount
WHERE id = :from_id;

UPDATE accounts
SET balance = balance + :amount
WHERE id = :to_id;

INSERT INTO transfers (from_account_id, to_account_id, amount, note)
VALUES (:from_id, :to_id, :amount, :'note');
COMMIT;

SELECT * FROM accounts ORDER BY id;
SELECT * FROM transfers ORDER BY id;
