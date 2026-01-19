package ru.itmo.se.is.cw.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import ru.itmo.se.is.cw.model.ClientApplicationEntity;

@Repository
public interface ClientApplicationRepository extends JpaRepository<ClientApplicationEntity, Long>, JpaSpecificationExecutor<ClientApplicationEntity> {
}
