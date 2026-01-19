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
@Table(name = "purchase_order_receipt")
public class PurchaseOrderReceiptEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @OneToOne(optional = false)
    @OnDelete(action = OnDeleteAction.RESTRICT)
    @JoinColumn(name = "purchase_order_id", nullable = false)
    private PurchaseOrderEntity purchaseOrder;

    @ManyToOne(optional = false)
    @OnDelete(action = OnDeleteAction.RESTRICT)
    @JoinColumn(name = "warehouse_worker_id", nullable = false)
    private EmployeeEntity warehouseWorker;

    @Column(name = "invoice_number", nullable = false, length = 20)
    private String invoiceNumber;

    @Column(name = "receipted_at", nullable = false)
    private ZonedDateTime receiptedAt;
}