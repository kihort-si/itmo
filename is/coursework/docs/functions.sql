CREATE OR REPLACE FUNCTION adjust_material_balance(
    p_material_id BIGINT,
    p_delta       REAL,
    p_changer_id  BIGINT
) RETURNS BIGINT LANGUAGE plpgsql AS $$
DECLARE v_prev BIGINT; v_prev_bal REAL; v_new_id BIGINT;
BEGIN
    SELECT current_balance_id INTO v_prev FROM material WHERE id = p_material_id FOR UPDATE;
    IF v_prev IS NULL THEN
        RAISE EXCEPTION 'Material % has no current balance', p_material_id;
    END IF;

    SELECT balance INTO v_prev_bal FROM material_balance WHERE id = v_prev;
    INSERT INTO material_balance(material_id, balance, previous_balance_id, changer_id)
    VALUES (p_material_id, v_prev_bal + p_delta, v_prev, p_changer_id)
    RETURNING id INTO v_new_id;

    RETURN v_new_id;
END $$;
-- операция изменения остатка

CREATE OR REPLACE FUNCTION consume_materials_for_order(
    p_order_id BIGINT,
    p_changer_account_id BIGINT
) RETURNS VOID LANGUAGE plpgsql AS $$
DECLARE v_design BIGINT; v_ca BIGINT; v_amount INT;
BEGIN
    SELECT product_design_id, client_application_id
    INTO v_design, v_ca
    FROM client_order WHERE id = p_order_id;

    SELECT amount INTO v_amount
    FROM client_application WHERE id = v_ca;

    INSERT INTO material_consumption(order_id, material_id, amount)
    SELECT p_order_id, rm.material_id, rm.amount * v_amount
    FROM required_material rm
    WHERE rm.product_design_id = v_design
    ON CONFLICT DO NOTHING;

    PERFORM adjust_material_balance(mc.material_id, -mc.amount, p_changer_account_id)
    FROM material_consumption mc
    WHERE mc.order_id = p_order_id;
END $$;
-- оформить потребление материалов по заказу

CREATE OR REPLACE FUNCTION add_client_order_status(p_order_id BIGINT, p_status VARCHAR)
    RETURNS BIGINT LANGUAGE plpgsql AS $$
DECLARE v_last TIMESTAMPTZ; v_new_id BIGINT;
BEGIN
    SELECT MAX(set_at) INTO v_last FROM client_order_status WHERE client_order_id = p_order_id;
    IF v_last IS NOT NULL AND v_last > now() THEN
        RAISE EXCEPTION 'Existing status is newer than now()';
    END IF;

    INSERT INTO client_order_status(client_order_id, status)
    VALUES (p_order_id, p_status)
    RETURNING id INTO v_new_id;

    RETURN v_new_id;
END $$;
-- обновить статус заказа клиента