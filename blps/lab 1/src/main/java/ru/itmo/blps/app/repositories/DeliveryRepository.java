package ru.itmo.blps.app.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.itmo.blps.app.models.Delivery;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
}
