package com.praneet.neo.repository;

import com.praneet.neo.model.Product;
import java.util.List;

public interface ProductRepository {
    List<Product> getProducts() throws Exception;
    List<Product> getProductsByCategory(String category) throws Exception;
    List<Product> searchProducts(String query) throws Exception;
} 