package com.praneet.neo.repository;

import com.praneet.neo.model.Product;
import com.praneet.neo.network.ProductApiService;
import com.praneet.neo.network.ProductListResponse;
import java.util.List;

public class ProductRepositoryImpl implements ProductRepository {
    private final ProductApiService api;

    public ProductRepositoryImpl(ProductApiService api) {
        this.api = api;
    }

    @Override
    public List<Product> getProducts() throws Exception {
        try {
            ProductListResponse response = api.getProducts().execute().body();
            return response != null ? response.getProducts() : null;
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public List<Product> getProductsByCategory(String category) throws Exception {
        try {
            // For now, get all products and filter by category
            List<Product> allProducts = getProducts();
            if (allProducts != null) {
                return allProducts.stream()
                    .filter(product -> category.equalsIgnoreCase(product.getCategory()))
                    .collect(java.util.stream.Collectors.toList());
            }
            return null;
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public List<Product> searchProducts(String query) throws Exception {
        try {
            ProductListResponse response = api.searchProducts(query).execute().body();
            return response != null ? response.getProducts() : null;
        } catch (Exception e) {
            throw e;
        }
    }
} 