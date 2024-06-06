CREATE INDEX IF NOT EXISTS ведомость_ид ON "Н_ВЕДОМОСТИ" USING btree("ИД");
CREATE INDEX IF NOT EXISTS ведомость_твид ON "Н_ВЕДОМОСТИ" USING btree("ТВ_ИД");
CREATE INDEX IF NOT EXISTS ведомость_члвк_ид ON "Н_ВЕДОМОСТИ" USING btree("ЧЛВК_ИД");
CREATE INDEX IF NOT EXISTS сессия_члвк_ид ON "Н_СЕССИЯ" USING btree("ЧЛВК_ИД");
CREATE INDEX IF NOT EXISTS люди_ид ON "Н_ЛЮДИ" USING btree("ИД");