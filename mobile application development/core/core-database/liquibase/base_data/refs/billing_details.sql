INSERT INTO refs.entity_list_schema (eschema_id, code, schema)
VALUES (
    4, 'bill_details',
    '{"code": "string", "name": "string", "description": "string"}'
);

INSERT INTO refs.entity_list (entity_list_id, schema_code, lang_id, data) VALUES 
(1, 'bill_details', 1, '{"code": "otherDebit", "name": "Other debits", "description": "Other debits"}'),
(2, 'bill_details', 1, '{"code": "otherCredit", "name": "Other credits", "description": "Other credits"}'),
(3, 'bill_details', 1, '{"code": "freeze", "name": "Freeze", "description": "Account freeze"}'),
(4, 'bill_details', 1, '{"code": "unfreeze", "name": "Unfreeze", "description": "Account unfreeze"}'),
(5, 'bill_details', 1, '{"code": "chargeStockPurchase", "name": "Stock Purchase", "description": "Purchase of stock"}'),
(6, 'bill_details', 1, '{"code": "payoutStockSell", "name": "Stock Sell", "description": "Payout for stock sale"}'),


(1, 'bill_details', 4, '{"code": "otherDebit", "name": "Прочие расходы", "description": "Прочие расходы"}'),
(2, 'bill_details', 4, '{"code": "otherCredit", "name": "Прочие поступления", "description": "Прочие поступления"}'),
(3, 'bill_details', 4, '{"code": "freeze", "name": "Заморозка средств", "description": "Заморозка средств"}'),
(4, 'bill_details', 4, '{"code": "unfreeze", "name": "Разморозка средств", "description": "Разморозка средств"}'),
(5, 'bill_details', 4, '{"code": "chargeStockPurchase", "name": "Покупка акций", "description": "Покупка акций"}'),
(6, 'bill_details', 4, '{"code": "payoutStockSell", "name": "Продажа акций", "description": "Выплата за продажу акций"}');