package com.praneet.neo.repository;

import com.praneet.neo.model.Product;
import com.praneet.neo.ProductDatabaseManager;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public class ProductRepositoryImpl implements ProductRepository {
    public ProductRepositoryImpl() {
        // No API dependency needed
    }

    @Override
    public List<Product> getProducts() throws Exception {
        return getProductsFromSupabase();
    }

    private List<Product> getProductsFromSupabase() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<List<Product>> result = new AtomicReference<>();
        AtomicReference<Exception> exception = new AtomicReference<>();

        ProductDatabaseManager.fetchProductsFromSupabase(new ProductDatabaseManager.ProductCallback() {
            @Override
            public void onSuccess(List<Product> products) {
                result.set(products);
                latch.countDown();
            }

            @Override
            public void onError(String error) {
                exception.set(new Exception(error));
                latch.countDown();
            }
        });

        latch.await();
        if (exception.get() != null) {
            throw exception.get();
        }
        return result.get();
    }

    @Override
    public List<Product> getProductsByCategory(String category) throws Exception {
        return getProductsByCategoryFromSupabase(category);
    }

    private List<Product> getProductsByCategoryFromSupabase(String category) throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<List<Product>> result = new AtomicReference<>();
        AtomicReference<Exception> exception = new AtomicReference<>();

        ProductDatabaseManager.searchProductsInSupabase(category, new ProductDatabaseManager.ProductCallback() {
            @Override
            public void onSuccess(List<Product> products) {
                // Filter by exact category match
                List<Product> filteredProducts = products.stream()
                    .filter(product -> category.equalsIgnoreCase(product.getCategory()))
                    .collect(java.util.stream.Collectors.toList());
                result.set(filteredProducts);
                latch.countDown();
            }

            @Override
            public void onError(String error) {
                exception.set(new Exception(error));
                latch.countDown();
            }
        });

        latch.await();
        if (exception.get() != null) {
            throw exception.get();
        }
        return result.get();
    }

    @Override
    public List<Product> searchProducts(String query) throws Exception {
        return searchProductsFromSupabase(query);
    }

    private List<Product> searchProductsFromSupabase(String query) throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<List<Product>> result = new AtomicReference<>();
        AtomicReference<Exception> exception = new AtomicReference<>();

        ProductDatabaseManager.searchProductsInSupabase(query, new ProductDatabaseManager.ProductCallback() {
            @Override
            public void onSuccess(List<Product> products) {
                result.set(products);
                latch.countDown();
            }

            @Override
            public void onError(String error) {
                exception.set(new Exception(error));
                latch.countDown();
            }
        });

        latch.await();
        if (exception.get() != null) {
            throw exception.get();
        }
        return result.get();
    }



    public Product getProductById(int productId) throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Product> result = new AtomicReference<>();
        AtomicReference<Exception> exception = new AtomicReference<>();

        ProductDatabaseManager.getProductById(productId, new ProductDatabaseManager.ProductCallback() {
            @Override
            public void onSuccess(List<Product> products) {
                if (products != null && !products.isEmpty()) {
                    result.set(products.get(0));
                }
                latch.countDown();
            }

            @Override
            public void onError(String error) {
                exception.set(new Exception(error));
                latch.countDown();
            }
        });

        latch.await();
        if (exception.get() != null) {
            throw exception.get();
        }
        return result.get();
    }

    public void updateProduct(Product product) throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Exception> exception = new AtomicReference<>();

        ProductDatabaseManager.updateProduct(product, new ProductDatabaseManager.DatabaseCallback() {
            @Override
            public void onSuccess(String message) {
                latch.countDown();
            }

            @Override
            public void onError(String error) {
                exception.set(new Exception(error));
                latch.countDown();
            }
        });

        latch.await();
        if (exception.get() != null) {
            throw exception.get();
        }
    }

    public void deleteProduct(int productId) throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Exception> exception = new AtomicReference<>();

        ProductDatabaseManager.deleteProduct(productId, new ProductDatabaseManager.DatabaseCallback() {
            @Override
            public void onSuccess(String message) {
                latch.countDown();
            }

            @Override
            public void onError(String error) {
                exception.set(new Exception(error));
                latch.countDown();
            }
        });

        latch.await();
        if (exception.get() != null) {
            throw exception.get();
        }
    }

    // Always use Supabase
    public void setUseSupabase(boolean useSupabase) {
        // No-op: always true
    }

    public boolean isUsingSupabase() {
        return true;
    }
} 