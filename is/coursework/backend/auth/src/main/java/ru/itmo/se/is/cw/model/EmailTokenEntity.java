package ru.itmo.se.is.cw.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.ZonedDateTime;

@Getter
@Setter
@Entity
@Table(name = "email_token")
public class EmailTokenEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "token", nullable = false)
    private String token;

    @ManyToOne(optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "account_id", nullable = false)
    private AccountEntity account;

    @Column(name = "expiration_dt", nullable = false)
    private ZonedDateTime expirationDt;

    @Column(name = "creation_dt", nullable = false)
    private ZonedDateTime creationDt;


}