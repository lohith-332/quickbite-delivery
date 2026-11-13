package com.quickbite.app.menu;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "food_items")
public class FoodItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false, length = 600)
    private String imageUrl;

    @Column(nullable = false)
    private boolean vegetarian;

    @Column(nullable = false)
    private boolean available = true;

    protected FoodItem() {
    }

    public FoodItem(String name, String description, String category, boolean vegetarian, BigDecimal price, String imageUrl) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.vegetarian = vegetarian;
        this.price = price;
        this.imageUrl = imageUrl;
        this.available = true;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public BigDecimal getPrice() { return price; }
    public String getImageUrl() { return imageUrl; }
    public boolean isVegetarian() { return vegetarian; }
    public boolean isAvailable() { return available; }
}
