package com.quickbite.app.menu;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/menu")
public class MenuController {
    private final FoodItemRepository foodItemRepository;

    public MenuController(FoodItemRepository foodItemRepository) {
        this.foodItemRepository = foodItemRepository;
    }

    @GetMapping
    public List<MenuItemResponse> getMenu() {
        return foodItemRepository.findByAvailableTrueOrderByCategoryAscNameAsc()
                .stream().map(MenuItemResponse::from).toList();
    }
}
