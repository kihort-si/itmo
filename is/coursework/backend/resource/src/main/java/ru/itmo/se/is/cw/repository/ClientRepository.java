package ru.itmo.se.is.cw.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import ru.itmo.se.is.cw.model.ClientEntity;

import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<ClientEntity, Long>, JpaSpecificationExecutor<ClientEntity> {
    Optional<ClientEntity> findByAccountId(Long accountId);
}
