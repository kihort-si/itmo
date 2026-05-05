TRUNCATE TABLE transfers, accounts RESTART IDENTITY CASCADE;

INSERT INTO accounts (owner_name, balance) VALUES
  ('Alice', 900.00),
  ('Bob', 900.00),
  ('Charlie', 1250.00);

INSERT INTO transfers (from_account_id, to_account_id, amount, note) VALUES
  (1, 2, 100.00, 'initial transfer'),
  (2, 3, 50.00, 'seed transfer');
