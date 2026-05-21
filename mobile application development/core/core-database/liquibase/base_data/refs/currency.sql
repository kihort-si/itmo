INSERT INTO refs.entity_list_schema (eschema_id, code, schema)
VALUES (
    3, 'currency',
    '{"code": "string", "name": "string", "symbol_code": "string"}'
);

INSERT INTO refs.entity_list (entity_list_id, schema_code, lang_id, data) VALUES 
(1, 'currency', 1, '{"code": "USD", "name": "US Dollar", "symbol_code": "$"}'),
(2, 'currency', 1, '{"code": "EUR", "name": "Euro", "symbol_code": "€"}'),
(3, 'currency', 1, '{"code": "GBP", "name": "British Pound", "symbol_code": "£"}'),
(4, 'currency', 1, '{"code": "JPY", "name": "Japanese Yen", "symbol_code": "¥"}'),
(5, 'currency', 1, '{"code": "CNY", "name": "Chinese Yuan", "symbol_code": "¥"}'),
(6, 'currency', 1, '{"code": "RUB", "name": "Russian Ruble", "symbol_code": "₽"}'),
(1, 'currency', 4, '{"code": "USD", "name": "Доллар США", "symbol_code": "$"}'),
(2, 'currency', 4, '{"code": "EUR", "name": "Евро", "symbol_code": "€"}'),
(3, 'currency', 4, '{"code": "GBP", "name": "Британский фунт", "symbol_code": "£"}'),
(4, 'currency', 4, '{"code": "JPY", "name": "Японская йена", "symbol_code": "¥"}'),
(5, 'currency', 4, '{"code": "CNY", "name": "Китайский юань", "symbol_code": "¥"}'),
(6, 'currency', 4, '{"code": "RUB", "name": "Российский рубль", "symbol_code": "₽"}');