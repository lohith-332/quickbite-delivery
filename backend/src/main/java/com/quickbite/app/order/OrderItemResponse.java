package com.quickbite.app.order;

import java.math.BigDecimal;

public record OrderItemResponse(Long menuItemId, String name, int quantity, BigDecimal unitPrice, BigDecimal lineTotal) {
    public static OrderItemResponse from(OrderItem item) {
        return new OrderItemResponse(item.getMenuItemId(), item.getItemName(), item.getQuantity(), item.getUnitPrice(),
                item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
    }
}
