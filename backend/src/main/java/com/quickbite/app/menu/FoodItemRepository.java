package com.quickbite.app.menu;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FoodItemRepository extends JpaRepository<FoodItem, Long> {
    List<FoodItem> findByAvailableTrueOrderByCategoryAscNameAsc();
}
