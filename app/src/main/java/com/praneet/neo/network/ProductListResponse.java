package com.praneet.neo.network;

import com.praneet.neo.model.Product;
import java.util.List;

public class ProductListResponse {
    private List<Product> products;
    private int total;
    private int skip;
    private int limit;

    // Default constructor for Gson
    public ProductListResponse() {
    }

    public ProductListResponse(List<Product> products, int total, int skip, int limit) {
        this.products = products;
        this.total = total;
        this.skip = skip;
        this.limit = limit;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getSkip() {
        return skip;
    }

    public void setSkip(int skip) {
        this.skip = skip;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }
} 