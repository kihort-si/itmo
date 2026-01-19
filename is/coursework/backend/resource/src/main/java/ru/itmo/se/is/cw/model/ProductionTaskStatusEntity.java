package ru.itmo.se.is.cw.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import ru.itmo.se.is.cw.model.value.ProductionTaskStatus;

import java.time.ZonedDateTime;

@Getter
@Setter
@Entity
@Table(name = "production_task_status")
public class ProductionTaskStatusEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "production_task_id", nullable = false)
    private ProductionTaskEntity productionTask;

    @Column(name = "status", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private ProductionTaskStatus status;

    @Column(name = "set_at", nullable = false)
    private ZonedDateTime setAt;
}