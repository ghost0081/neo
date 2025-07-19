package com.praneet.neo.model;

import java.util.List;

public class Product {
    private int id;
    private String title;
    private String description;
    private double price;
    private double discountPercentage;
    private double rating;
    private int stock;
    private String brand;
    private String category;
    private String thumbnail;
    private List<String> images;

    // Default constructor for Gson
    public Product() {
    }

    public Product(int id, String title, String description, double price, 
                   double discountPercentage, double rating, int stock, 
                   String brand, String category, String thumbnail, List<String> images) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.price = price;
        this.discountPercentage = discountPercentage;
        this.rating = rating;
        this.stock = stock;
        this.brand = brand;
        this.category = category;
        this.thumbnail = thumbnail;
        this.images = images;
    }

    // Getters
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }
    public double getDiscountPercentage() { return discountPercentage; }
    public double getRating() { return rating; }
    public int getStock() { return stock; }
    public String getBrand() { return brand; }
    public String getCategory() { return category; }
    public String getThumbnail() { return thumbnail; }
    public List<String> getImages() { return images; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setPrice(double price) { this.price = price; }
    public void setDiscountPercentage(double discountPercentage) { this.discountPercentage = discountPercentage; }
    public void setRating(double rating) { this.rating = rating; }
    public void setStock(int stock) { this.stock = stock; }
    public void setBrand(String brand) { this.brand = brand; }
    public void setCategory(String category) { this.category = category; }
    public void setThumbnail(String thumbnail) { this.thumbnail = thumbnail; }
    public void setImages(List<String> images) { this.images = images; }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Product product = (Product) obj;
        return id == product.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", price=" + price +
                '}';
    }
} 