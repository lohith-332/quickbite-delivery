package com.quickbite.app.payment;

public record CreatePaymentOrderResponse(
        Long orderId,
        String razorpayOrderId,
        long amount,
        String currency,
        String keyId
) {
}
