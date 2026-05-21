CREATE OR REPLACE FUNCTION balm.scheduled_report_trigger_fn()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
DECLARE
    target_attr_id CONSTANT INTEGER := 4;
    target_report_type CONSTANT VARCHAR := 'DAILY_ACCOUNT_BALANCE_REPORT';
BEGIN
    -- Обработка INSERT
    IF TG_OP = 'INSERT' THEN
        IF NEW.attribute_refs_id = target_attr_id THEN
            INSERT INTO balm.scheduled_report (clnt_id, report_type, schedule, start_date, end_date)
            VALUES (
                NEW.clnt_id,
                target_report_type,
                jsonb_build_object('hour', NEW.value::INT),
                NEW.start_date,
                NEW.end_date
            )
            ON CONFLICT (clnt_id, report_type) DO UPDATE SET
                schedule = EXCLUDED.schedule,
                start_date = EXCLUDED.start_date,
                end_date = EXCLUDED.end_date;
        END IF;

    -- Обработка UPDATE
    ELSIF TG_OP = 'UPDATE' THEN
        -- Если старый атрибут был целевым, и он изменился или изменился clnt_id — удаляем старую запись отчёта
        IF OLD.attribute_refs_id = target_attr_id THEN
            IF NEW.attribute_refs_id != target_attr_id OR OLD.clnt_id != NEW.clnt_id THEN
                DELETE FROM balm.scheduled_report
                WHERE clnt_id = OLD.clnt_id AND report_type = target_report_type;
            END IF;
        END IF;

        -- Если новый атрибут целевой — вставляем/обновляем отчёт для нового clnt_id
        IF NEW.attribute_refs_id = target_attr_id THEN
            INSERT INTO balm.scheduled_report (clnt_id, report_type, schedule, start_date, end_date)
            VALUES (
                NEW.clnt_id,
                target_report_type,
                jsonb_build_object('hour', NEW.value::INT),
                NEW.start_date,
                NEW.end_date
            )
            ON CONFLICT (clnt_id, report_type) DO UPDATE SET
                schedule = EXCLUDED.schedule,
                start_date = EXCLUDED.start_date,
                end_date = EXCLUDED.end_date;
        END IF;

    -- Обработка DELETE
    ELSIF TG_OP = 'DELETE' THEN
        IF OLD.attribute_refs_id = target_attr_id THEN
            DELETE FROM balm.scheduled_report
            WHERE clnt_id = OLD.clnt_id AND report_type = target_report_type;
        END IF;
    END IF;

    RETURN NULL; -- Для триггера AFTER результат игнорируется
END;
$$;

-- Создание триггера (срабатывает после изменения строки)
CREATE TRIGGER scheduled_report_trigger
AFTER INSERT OR UPDATE OR DELETE ON balm.client_attribute
FOR EACH ROW
EXECUTE FUNCTION balm.scheduled_report_trigger_fn();