package ru.itmo.se.is.cw.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import ru.itmo.se.is.cw.model.ProductDesignEntity;

@Repository
public interface ProductDesignRepository extends JpaRepository<ProductDesignEntity, Long>, JpaSpecificationExecutor<ProductDesignEntity> {
}
