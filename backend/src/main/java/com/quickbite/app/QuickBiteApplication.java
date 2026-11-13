package com.quickbite.app;

import com.quickbite.app.menu.FoodItem;
import com.quickbite.app.menu.FoodItemRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.math.BigDecimal;
import java.util.List;

@SpringBootApplication
public class QuickBiteApplication {

    public static void main(String[] args) {
        SpringApplication.run(QuickBiteApplication.class, args);
    }

    @Bean
    CommandLineRunner seedMenu(FoodItemRepository foodItemRepository) {
        return args -> {
            if (foodItemRepository.count() > 0) {
                return;
            }

            foodItemRepository.saveAll(List.of(
                    new FoodItem("Classic Smash Burger", "Double-seared patty, cheddar, pickles and house sauce.", "Burgers", false, new BigDecimal("249.00"), "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?auto=format&fit=crop&w=1000&q=85"),
                    new FoodItem("Crispy Paneer Burger", "Spiced paneer, crunchy slaw and mint yogurt.", "Burgers", true, new BigDecimal("219.00"), "https://images.unsplash.com/photo-1550547660-d9450f859349?auto=format&fit=crop&w=1000&q=85"),
                    new FoodItem("Garden Margherita", "Stone-baked crust, tomato, basil and mozzarella.", "Pizza", true, new BigDecimal("329.00"), "https://images.unsplash.com/photo-1579751626657-72bc17010498?auto=format&fit=crop&w=1000&q=85"),
                    new FoodItem("Spicy Chicken Pizza", "Roast chicken, jalapeño, red onion and chilli oil.", "Pizza", false, new BigDecimal("389.00"), "https://images.unsplash.com/photo-1513104890138-7c749659a591?auto=format&fit=crop&w=1000&q=85"),
                    new FoodItem("Creamy Alfredo Pasta", "Silky parmesan sauce, mushrooms and fresh herbs.", "Pasta", true, new BigDecimal("299.00"), "https://images.unsplash.com/photo-1473093295043-cdd812d0e601?auto=format&fit=crop&w=1000&q=85"),
                    new FoodItem("Chilli Garlic Noodles", "Wok-tossed noodles with vegetables and toasted sesame.", "Asian", true, new BigDecimal("259.00"), "https://images.unsplash.com/photo-1569718212165-3a8278d5f624?auto=format&fit=crop&w=1000&q=85"),
                    new FoodItem("Crispy Masala Fries", "Golden fries dusted with our tangy house masala.", "Sides", true, new BigDecimal("129.00"), "https://images.unsplash.com/photo-1573080496219-bb080dd4f877?auto=format&fit=crop&w=1000&q=85"),
                    new FoodItem("Chocolate Fudge Brownie", "Warm, rich chocolate brownie with a soft centre.", "Desserts", true, new BigDecimal("159.00"), "https://images.unsplash.com/photo-1606313564200-e75d5e30476c?auto=format&fit=crop&w=1000&q=85")
            ));
        };
    }
}
