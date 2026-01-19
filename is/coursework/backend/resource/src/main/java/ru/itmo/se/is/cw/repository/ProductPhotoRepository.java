package ru.itmo.se.is.cw.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.itmo.se.is.cw.model.ProductPhotoEntity;

@Repository
public interface ProductPhotoRepository extends JpaRepository<ProductPhotoEntity, Long> {
    Page<ProductPhotoEntity> findByProductCatalogId(Long productCatalogId, Pageable pageable);
}
