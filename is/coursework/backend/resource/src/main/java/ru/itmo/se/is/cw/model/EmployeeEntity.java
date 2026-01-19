package ru.itmo.se.is.cw.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import ru.itmo.se.is.cw.model.value.EmployeeRole;

@Getter
@Setter
@Entity
@Table(name = "employee")
public class EmployeeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @OneToOne(optional = false, cascade = CascadeType.ALL)
    @OnDelete(action = OnDeleteAction.RESTRICT)
    @JoinColumn(name = "person_id", nullable = false)
    private PersonEntity person;

    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    private EmployeeRole role;
}