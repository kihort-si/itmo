package ru.itmo.blps.app.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.itmo.blps.app.models.PromoCode;

public interface PromoCodeRepository extends JpaRepository<PromoCode, String> {
}
