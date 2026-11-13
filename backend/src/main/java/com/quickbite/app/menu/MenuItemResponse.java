package com.quickbite.app.menu;

import java.math.BigDecimal;

public record MenuItemResponse(
        Long id,
        String name,
        String description,
        String category,
        BigDecimal price,
        String imageUrl,
        boolean vegetarian
) {
    public static MenuItemResponse from(FoodItem foodItem) {
        return new MenuItemResponse(foodItem.getId(), foodItem.getName(), foodItem.getDescription(),
                foodItem.getCategory(), foodItem.getPrice(), foodItem.getImageUrl(), foodItem.isVegetarian());
    }
}
