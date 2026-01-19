package ru.itmo.se.is.cw.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import ru.itmo.se.is.cw.model.value.ClientOrderStatus;

import java.time.ZonedDateTime;

@Getter
@Setter
@Entity
@Table(name = "client_order_status")
public class ClientOrderStatusEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "client_order_id", nullable = false)
    private ClientOrderEntity clientOrder;

    @Column(name = "status", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private ClientOrderStatus status;

    @Column(name = "set_at", nullable = false)
    private ZonedDateTime setAt;


}