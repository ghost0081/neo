package com.praneet.neo.viewmodel;

import com.praneet.neo.model.Product;
import java.util.List;

public abstract class HomeUiState {
    public static class Loading extends HomeUiState {}
    
    public static class Success extends HomeUiState {
        private List<Product> products;
        
        public Success(List<Product> products) {
            this.products = products;
        }
        
        public List<Product> getProducts() {
            return products;
        }
    }
    
    public static class Error extends HomeUiState {
        private String message;
        
        public Error(String message) {
            this.message = message;
        }
        
        public String getMessage() {
            return message;
        }
    }
} 