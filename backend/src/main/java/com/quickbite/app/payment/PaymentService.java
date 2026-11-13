package com.quickbite.app.payment;

import com.fasterxml.jackson.databind.JsonNode;
import com.quickbite.app.order.CreateOrderRequest;
import com.quickbite.app.order.CustomerOrder;
import com.quickbite.app.order.CustomerOrderRepository;
import com.quickbite.app.order.OrderResponse;
import com.quickbite.app.order.OrderService;
import com.quickbite.app.order.PaymentStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Map;

@Service
public class PaymentService {
    private static final String RAZORPAY_API = "https://api.razorpay.com/v1";
    private static final String CURRENCY = "INR";

    private final OrderService orderService;
    private final CustomerOrderRepository orderRepository;
    private final RestClient razorpayClient = RestClient.create();

    @Value("${razorpay.key-id:}")
    private String keyId;

    @Value("${razorpay.key-secret:}")
    private String keySecret;

    public PaymentService(OrderService orderService, CustomerOrderRepository orderRepository) {
        this.orderService = orderService;
        this.orderRepository = orderRepository;
    }

    @Transactional
    public CreatePaymentOrderResponse createPaymentOrder(CreateOrderRequest request) {
        requireCredentials();
        CustomerOrder order = orderService.placePendingPaymentOrder(request);
        long amountInPaise = toPaise(order.getTotalAmount());

        try {
            JsonNode response = razorpayClient.post()
                    .uri(RAZORPAY_API + "/orders")
                    .headers(headers -> headers.setBasicAuth(keyId, keySecret))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "amount", amountInPaise,
                            "currency", CURRENCY,
                            "receipt", "quickbite-" + order.getId(),
                            "notes", Map.of("quickbite_order_id", order.getId().toString())
                    ))
                    .retrieve()
                    .body(JsonNode.class);

            String razorpayOrderId = response == null || response.get("id") == null
                    ? null : response.get("id").asText();
            if (razorpayOrderId == null || razorpayOrderId.isBlank()) {
                throw new IllegalStateException("Razorpay did not return a payment order. Please try again.");
            }

            order.setRazorpayOrderId(razorpayOrderId);
            return new CreatePaymentOrderResponse(order.getId(), razorpayOrderId, amountInPaise, CURRENCY, keyId);
        } catch (RestClientException exception) {
            throw new IllegalStateException("Could not connect to Razorpay. Check your test keys and internet connection, then try again.");
        }
    }

    @Transactional
    public OrderResponse verifyPayment(VerifyPaymentRequest request) {
        requireCredentials();
        CustomerOrder order = orderRepository.findById(request.orderId())
                .orElseThrow(() -> new IllegalArgumentException("The QuickBite order was not found."));

        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            if (request.razorpayPaymentId().equals(order.getRazorpayPaymentId())) {
                return OrderResponse.from(order);
            }
            throw new IllegalArgumentException("This QuickBite order has already been paid.");
        }
        if (order.getPaymentStatus() != PaymentStatus.PENDING
                || !request.razorpayOrderId().equals(order.getRazorpayOrderId())) {
            throw new IllegalArgumentException("The payment does not match this QuickBite order.");
        }

        verifyCheckoutSignature(request);
        verifyPaymentWithRazorpay(order, request.razorpayPaymentId());
        order.markPaid(request.razorpayPaymentId());
        return OrderResponse.from(order);
    }

    private void verifyCheckoutSignature(VerifyPaymentRequest request) {
        try {
            String signedPayload = request.razorpayOrderId() + "|" + request.razorpayPaymentId();
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(keySecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            String expected = HexFormat.of().formatHex(mac.doFinal(signedPayload.getBytes(StandardCharsets.UTF_8)));
            boolean matches = MessageDigest.isEqual(
                    expected.getBytes(StandardCharsets.US_ASCII),
                    request.razorpaySignature().getBytes(StandardCharsets.US_ASCII));
            if (!matches) {
                throw new IllegalArgumentException("Razorpay payment verification failed. No payment was recorded.");
            }
        } catch (IllegalArgumentException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IllegalStateException("Could not verify the Razorpay payment response.");
        }
    }

    private void verifyPaymentWithRazorpay(CustomerOrder order, String paymentId) {
        try {
            JsonNode payment = razorpayClient.get()
                    .uri(RAZORPAY_API + "/payments/{paymentId}", paymentId)
                    .headers(headers -> headers.setBasicAuth(keyId, keySecret))
                    .retrieve()
                    .body(JsonNode.class);

            if (payment == null
                    || !order.getRazorpayOrderId().equals(text(payment, "order_id"))
                    || toPaise(order.getTotalAmount()) != number(payment, "amount")
                    || !CURRENCY.equals(text(payment, "currency"))
                    || !"captured".equals(text(payment, "status"))) {
                throw new IllegalArgumentException("Razorpay has not confirmed a captured payment for this order.");
            }
        } catch (RestClientException exception) {
            throw new IllegalStateException("Could not confirm the payment with Razorpay. Please retry verification.");
        }
    }

    private void requireCredentials() {
        if (keyId == null || !keyId.startsWith("rzp_test_") || keySecret == null || keySecret.isBlank()) {
            throw new IllegalStateException("Add your Razorpay test Key ID and Key Secret to the backend run configuration first.");
        }
    }

    private long toPaise(BigDecimal amount) {
        return amount.multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.UNNECESSARY).longValueExact();
    }

    private String text(JsonNode node, String field) {
        return node.hasNonNull(field) ? node.get(field).asText() : "";
    }

    private long number(JsonNode node, String field) {
        return node.hasNonNull(field) ? node.get(field).asLong(-1) : -1;
    }
}
