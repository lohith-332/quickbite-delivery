package com.quickbite.app.payment;

import com.quickbite.app.order.CreateOrderRequest;
import com.quickbite.app.order.OrderResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/create-order")
    @ResponseStatus(HttpStatus.CREATED)
    public CreatePaymentOrderResponse createOrder(@Valid @RequestBody CreateOrderRequest request) {
        return paymentService.createPaymentOrder(request);
    }

    @PostMapping("/verify")
    public OrderResponse verify(@Valid @RequestBody VerifyPaymentRequest request) {
        return paymentService.verifyPayment(request);
    }
}
