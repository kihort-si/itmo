CREATE OR REPLACE FUNCTION balm.process_charge(
    p_acc_id INTEGER,
    p_chtp_id INTEGER,
    p_amount DECIMAL,
    p_bdet_id INTEGER
) 
RETURNS SETOF balm.charge AS 
$$
DECLARE
    v_balance DECIMAL;
    v_charge balm.charge%ROWTYPE;
BEGIN
    -- Получаем текущий баланс с блокировкой для предотвращения гонок
    SELECT balance INTO v_balance
    FROM balm.balance_cache
    WHERE acc_id = p_acc_id
    FOR UPDATE;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Balance cache not found for account %', p_acc_id;
    END IF;

    -- Проверка на отрицательный баланс для списаний и заморозок
    IF p_chtp_id IN (SELECT chtp_id FROM balm.charge_types WHERE code IN ('debit', 'freeze')) THEN
        IF v_balance < p_amount THEN
            RAISE EXCEPTION 'Insufficient balance';
        END IF;
    END IF;

    -- Вставка charge
    INSERT INTO balm.charge (acc_id, chtp_id, amount, bdet_id)
    VALUES (p_acc_id, p_chtp_id, p_amount, p_bdet_id)
    RETURNING * INTO v_charge;

    -- Обновление баланса
    UPDATE balm.balance_cache
    SET balance = balance + 
        CASE 
            WHEN p_chtp_id IN (SELECT chtp_id FROM balm.charge_types WHERE code IN ('credit', 'unfreeze')) THEN p_amount
            WHEN p_chtp_id IN (SELECT chtp_id FROM balm.charge_types WHERE code IN ('debit', 'freeze')) THEN -p_amount
            ELSE 0
        END
    WHERE acc_id = p_acc_id;

    RETURN NEXT v_charge;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION balm.recalculate_balance(p_acc_id INTEGER)
RETURNS DECIMAL AS $$
DECLARE
    new_balance DECIMAL;
BEGIN
    SELECT COALESCE(SUM(
        CASE 
            WHEN chtp.code IN ('credit', 'unfreeze') THEN amount
            WHEN chtp.code IN ('debit', 'freeze') THEN -amount
            ELSE 0
        END
    ), 0)
    INTO new_balance
    FROM balm.charge
    JOIN balm.charge_types chtp USING (chtp_id)
    WHERE acc_id = p_acc_id;

    UPDATE balm.balance_cache
    SET balance = new_balance
    WHERE acc_id = p_acc_id;

    RETURN new_balance;
END;
$$ LANGUAGE plpgsql;