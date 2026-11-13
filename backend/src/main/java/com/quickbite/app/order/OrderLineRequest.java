package com.quickbite.app.order;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record OrderLineRequest(
        @NotNull Long itemId,
        @Min(1) int quantity
) {
}
