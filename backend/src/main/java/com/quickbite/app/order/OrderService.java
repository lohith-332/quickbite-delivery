package com.quickbite.app.order;

import com.quickbite.app.menu.FoodItem;
import com.quickbite.app.menu.FoodItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class OrderService {
    private final CustomerOrderRepository customerOrderRepository;
    private final FoodItemRepository foodItemRepository;

    public OrderService(CustomerOrderRepository customerOrderRepository, FoodItemRepository foodItemRepository) {
        this.customerOrderRepository = customerOrderRepository;
        this.foodItemRepository = foodItemRepository;
    }

    @Transactional
    public CustomerOrder placePendingPaymentOrder(CreateOrderRequest request) {
        CustomerOrder order = buildOrder(request);
        order.setStatus(OrderStatus.PAYMENT_PENDING);
        return customerOrderRepository.save(order);
    }

    private CustomerOrder buildOrder(CreateOrderRequest request) {
        CustomerOrder order = new CustomerOrder(request.fullName().trim(), request.email().trim(), request.deliveryAddress().trim());
        BigDecimal total = BigDecimal.ZERO;

        for (OrderLineRequest requestedLine : request.items()) {
            FoodItem foodItem = foodItemRepository.findById(requestedLine.itemId())
                    .orElseThrow(() -> new IllegalArgumentException("A menu item in your cart could not be found."));
            if (!foodItem.isAvailable()) {
                throw new IllegalArgumentException(foodItem.getName() + " is currently unavailable.");
            }

            order.addItem(new OrderItem(foodItem.getId(), foodItem.getName(), requestedLine.quantity(), foodItem.getPrice()));
            total = total.add(foodItem.getPrice().multiply(BigDecimal.valueOf(requestedLine.quantity())));
        }

        order.setTotalAmount(total);
        return order;
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(Long orderId) {
        CustomerOrder order = customerOrderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order " + orderId + " was not found."));
        return OrderResponse.from(order);
    }
}
