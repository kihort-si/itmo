package ru.itmo.blps.app.repositories;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.itmo.blps.app.models.AppUser;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByUsername(String username);
}
