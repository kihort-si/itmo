package ru.itmo.blps.app.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.itmo.blps.app.models.Courier;

public interface CourierRepository extends JpaRepository<Courier, Long> {
}
