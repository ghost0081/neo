package com.praneet.neo.network;

import com.praneet.neo.model.Product;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ProductApiService {
    @GET("products")
    Call<ProductListResponse> getProducts();
    
    @GET("products/search")
    Call<ProductListResponse> searchProducts(@Query("q") String query);
} 