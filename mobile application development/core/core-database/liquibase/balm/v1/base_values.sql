INSERT INTO balm.account_state (accst_id, name) VALUES
(1, 'active'),
(2, 'frozen'),
(3, 'closed');

INSERT INTO balm.bill_details (bdet_id, code, bdet_refs_id) VALUES
(1,'otherDebit', 1),
(2,'otherCredit', 2),
(3,'freeze', 3),
(4,'unfreeze', 4);

INSERT INTO balm.charge_types (chtp_id,code, default_bdet_id) VALUES
(1,'debit', 1),
(2,'credit', 2),
(3,'freeze', 3),
(4,'unfreeze', 4);

INSERT INTO balm.scheme_type (scht_id, code) VALUES
(1, 'СalculationScheme'),
(2, 'СommissionScheme');