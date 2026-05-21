INSERT INTO refs.entity_list_schema (eschema_id, code, schema)
VALUES (
    2, 'attributes',
    '{"code": "string", "name": "string", "description": "string", "type": "string", "properties": "object"}'
);

INSERT INTO refs.entity_list (entity_list_id, schema_code, lang_id, data) VALUES 
(1, 'attributes', 1, '{"code": "traderLicenseR1", "name": "Trader License Rank 1", "description": "Special trader license, rank 1", "type": "license", "properties": {}}'),
(2, 'attributes', 1, '{"code": "traderLicenseR2", "name": "Trader License Rank 2", "description": "Special trader license, rank 2", "type": "license", "properties": {}}'),
(3, 'attributes', 1, '{"code": "traderLicenseR3", "name": "Trader License Rank 3", "description": "Special trader license, rank 3", "type": "license", "properties": {}}'),
(1, 'attributes', 4, '{"code": "traderLicenseR1", "name": "Лицензия трейдера первого ранга", "description": "Специальная лицензия трейдера, ранг 1", "type": "license", "properties": {}}'),
(2, 'attributes', 4, '{"code": "traderLicenseR2", "name": "Лицензия трейдера второго ранга", "description": "Специальная лицензия трейдера, ранг 2", "type": "license", "properties": {}}'),
(3, 'attributes', 4, '{"code": "traderLicenseR3", "name": "Лицензия трейдера третьего ранга", "description": "Специальная лицензия трейдера, ранг 3", "type": "license", "properties": {}}');

INSERT INTO refs.entity_list (entity_list_id, schema_code, lang_id, data) VALUES 
(4, 'attributes', 1, '{"code": "dailyReportHour", "name": "Daily Report Hour", "description": "Hour for daily reports", "type": "setting", "properties": {"type": "selectIntNumber", "min": 0, "max": 23}}'),
(4, 'attributes', 4, '{"code": "dailyReportHour", "name": "Час для ежедневных отчетов", "description": "Час для ежедневных отчетов", "type": "setting", "properties": {"type": "selectIntNumber", "min": 0, "max": 23}}');