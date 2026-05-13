package ru.itmo.blps.app.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.itmo.blps.app.models.ProcessedTaigaStatusEvent;

public interface ProcessedTaigaStatusEventRepository extends JpaRepository<ProcessedTaigaStatusEvent, String> {
}
