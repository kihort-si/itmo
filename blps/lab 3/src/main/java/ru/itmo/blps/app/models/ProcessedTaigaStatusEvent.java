package ru.itmo.blps.app.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "processed_taiga_status_events", uniqueConstraints = {
        @UniqueConstraint(name = "uk_processed_taiga_event", columnNames = {"event_id"})
})
@Getter
@Setter
@NoArgsConstructor
public class ProcessedTaigaStatusEvent {

    @Id
    @Column(name = "event_id", length = 64, nullable = false)
    private String eventId;

    @Column(nullable = false)
    private Instant processedAt;

    public ProcessedTaigaStatusEvent(String eventId) {
        this.eventId = eventId;
        this.processedAt = Instant.now();
    }
}
