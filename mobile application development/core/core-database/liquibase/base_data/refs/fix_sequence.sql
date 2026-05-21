SET search_path TO refs;
select setval('entity_list_entl_id_seq', (SELECT MAX(entl_id) FROM refs.entity_list));
select setval('entity_list_schema_eschema_id_seq', (SELECT MAX(eschema_id) FROM refs.entity_list_schema));
select setval('entity_single_ents_id_seq', (SELECT MAX(ents_id) FROM refs.entity_single));
select setval('language_lang_id_seq', (SELECT MAX(lang_id) FROM refs.language));