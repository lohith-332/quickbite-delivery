package com.quickbite.app.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long orderId,
        String fullName,
        String email,
        String deliveryAddress,
        OrderStatus status,
        PaymentStatus paymentStatus,
        BigDecimal total,
        LocalDateTime createdAt,
        List<OrderItemResponse> items
) {
    public static OrderResponse from(CustomerOrder order) {
        return new OrderResponse(order.getId(), order.getFullName(), order.getEmail(), order.getDeliveryAddress(),
                order.getStatus(), order.getPaymentStatus(), order.getTotalAmount(), order.getCreatedAt(),
                order.getItems().stream().map(OrderItemResponse::from).toList());
    }
}
