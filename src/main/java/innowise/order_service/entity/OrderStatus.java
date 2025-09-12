package innowise.order_service.entity;

public enum OrderStatus {
    CREATING,
    NEW,
    PAYMENT_WAITING,
    PAYMENT_RECEIVED,
    PROCESSING,
    DELIVERED,
    CANCELLED,
    PAYMENT_FAILED
}
