package ru.itmo.se.is.cw.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.itmo.se.is.cw.model.PurchaseOrderReceiptEntity;

@Repository
public interface PurchaseOrderReceiptRepository extends JpaRepository<PurchaseOrderReceiptEntity, Long> {
}
