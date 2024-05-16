CREATE OR REPLACE FUNCTION update_eal_status()
RETURNS TRIGGER
    LANGUAGE plpgsql
    AS $$
BEGIN
    UPDATE eal SET status = 3 WHERE eal.id IN (
        SELECT eal.id FROM eal
            JOIN eal_user ON eal.id = eal_user.eal_id
            JOIN person ON eal_user.person_id = person.id
            JOIN person_with_job ON person.id = person_with_job.person_id
            JOIN job ON person_with_job.job_id = job.id
            WHERE job.id NOT IN (1, 3) AND eal.status = 1 AND eal_user.person_id = NEW.person_id
        );
    RETURN NEW;
END
$$;

CREATE OR REPLACE TRIGGER check_job_id
AFTER INSERT OR UPDATE OF person_id ON eal_user
FOR EACH ROW
EXECUTE PROCEDURE update_eal_status();
