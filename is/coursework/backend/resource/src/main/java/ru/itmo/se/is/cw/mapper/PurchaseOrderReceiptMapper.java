package ru.itmo.se.is.cw.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.itmo.se.is.cw.conf.MapperConfig;
import ru.itmo.se.is.cw.dto.PurchaseOrderReceiptRequest;
import ru.itmo.se.is.cw.dto.PurchaseOrderReceiptResponseDto;
import ru.itmo.se.is.cw.model.EmployeeEntity;
import ru.itmo.se.is.cw.model.PurchaseOrderEntity;
import ru.itmo.se.is.cw.model.PurchaseOrderReceiptEntity;

@Mapper(config = MapperConfig.class)
public interface PurchaseOrderReceiptMapper {

    @Mapping(target = "purchaseOrderId", source = "purchaseOrder.id")
    @Mapping(target = "warehouseWorkerId", source = "warehouseWorker.id")
    PurchaseOrderReceiptResponseDto toDto(PurchaseOrderReceiptEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "purchaseOrder", source = "purchaseOrder")
    @Mapping(target = "warehouseWorker", source = "warehouseWorker")
    @Mapping(target = "invoiceNumber", source = "request.invoiceNumber")
    @Mapping(target = "receiptedAt", expression = "java(java.time.ZonedDateTime.now())")
    PurchaseOrderReceiptEntity toEntity(
            PurchaseOrderReceiptRequest request,
            PurchaseOrderEntity purchaseOrder,
            EmployeeEntity warehouseWorker
    );
}
