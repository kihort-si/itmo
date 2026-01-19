package ru.itmo.se.is.cw.model.value;

public enum ClientOrderStatus {
    CREATED,
    IN_PROGRESS,
    CONSTRUCTOR_PENDING_APPROVAL,
    CLIENT_PENDING_APPROVAL,
    REWORK,
    CLIENT_REWORK,
    READY_FOR_PRODUCTION,
    IN_PRODUCTION,
    READY_FOR_PICKUP,
    COMPLETED
}
