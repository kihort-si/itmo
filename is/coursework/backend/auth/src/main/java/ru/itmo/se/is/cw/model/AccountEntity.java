package ru.itmo.se.is.cw.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import ru.itmo.se.is.cw.model.value.AccountRole;

import java.time.ZonedDateTime;

@Getter
@Setter
@Entity
@Table(name = "account")
public class AccountEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "username", nullable = false, length = 50)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "enabled", nullable = false)
    private Boolean enabled;

    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    private AccountRole role;

    @Column(name = "updated_at", nullable = false)
    @UpdateTimestamp
    private ZonedDateTime updatedAt;

    @Column(name = "created_at", nullable = false)
    @CreationTimestamp
    private ZonedDateTime createdAt;
}