package com.quickbite.app.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CreateOrderRequest(
        @NotBlank String fullName,
        @NotBlank @Email String email,
        @NotBlank String deliveryAddress,
        @NotEmpty List<@Valid OrderLineRequest> items
) {
}
