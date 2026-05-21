INSERT INTO refs.entity_list_schema (eschema_id, code, schema)
VALUES (
    1, 'regions',
    '{"identifier": "integer", "code": "string", "name": "string"}'
);

INSERT INTO refs.entity_list (entity_list_id, schema_code, lang_id, data) VALUES 
(1, 'regions', 1, '{"identifier": 1, "code": "TEST", "name": "Test area"}'),
(2, 'regions', 1, '{"identifier": 2, "code": "MAIN", "name": "Main region"}');