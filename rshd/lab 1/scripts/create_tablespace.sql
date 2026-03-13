CREATE TABLE public.fact_events
(
    id         bigserial PRIMARY KEY,
    event_time timestamptz    NOT NULL,
    user_id    int            NOT NULL,
    amount     numeric(12, 2) NOT NULL,
    payload    text
);

INSERT INTO public.fact_events(event_time, user_id, amount, payload)
SELECT now() - (random() * interval '30 days'),
       (random() * 100000)::int,
    round((random() * 10000)::numeric, 2),
       md5(random()::text)
FROM generate_series(1, 200000);

CREATE INDEX fact_events_event_time_idx
    ON public.fact_events (event_time)
    TABLESPACE ts_idx;

CREATE INDEX fact_events_user_id_idx
    ON public.fact_events (user_id)
    TABLESPACE ts_idx;
