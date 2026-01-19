package ru.itmo.se.is.cw.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import ru.itmo.se.is.cw.model.value.ConversationStatus;

import java.time.ZonedDateTime;

@Getter
@Setter
@Entity
@Table(name = "conversation")
public class ConversationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @OneToOne(optional = false)
    @OnDelete(action = OnDeleteAction.RESTRICT)
    @JoinColumn(name = "order_id", nullable = false)
    private ClientOrderEntity order;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private ConversationStatus status;

    @Column(name = "created_at", nullable = false)
    @CreationTimestamp
    private ZonedDateTime createdAt;
}