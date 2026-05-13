package ru.itmo.blps.audit;

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
@Table(name = "order_status_audit", schema = "audit", uniqueConstraints = {
        @UniqueConstraint(name = "uk_order_status_audit_event", columnNames = {"event_id"})
})
@Getter
@Setter
@NoArgsConstructor
public class OrderStatusAuditEntry {

    @Id
    @Column(name = "event_id", length = 64, nullable = false)
    private String eventId;

    @Column(nullable = false)
    private long orderId;

    @Column(nullable = false, length = 64)
    private String status;

    @Column(nullable = false)
    private boolean paid;

    @Column(nullable = false, length = 48)
    private String occurredAt;

    @Column(nullable = false)
    private Instant storedAt;

    public OrderStatusAuditEntry(String eventId, long orderId, String status, boolean paid, String occurredAt) {
        this.eventId = eventId;
        this.orderId = orderId;
        this.status = status;
        this.paid = paid;
        this.occurredAt = occurredAt;
        this.storedAt = Instant.now();
    }
}
