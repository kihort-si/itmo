package ru.itmo.se.is.cw.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Setter
@Entity
@Table(name = "client")
public class ClientEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "email", nullable = false, length = 50)
    private String email;

    @OneToOne(optional = false, cascade = CascadeType.ALL)
    @OnDelete(action = OnDeleteAction.RESTRICT)
    @JoinColumn(name = "person_id", nullable = false)
    private PersonEntity person;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;


}