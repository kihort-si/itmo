package ru.itmo.blps.app.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.itmo.blps.app.models.ShopAssistant;

public interface ShopAssistantRepository extends JpaRepository<ShopAssistant, Long> {
}
