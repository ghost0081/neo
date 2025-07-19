package com.praneet.neo.network;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NetworkManager {
    private static final String BASE_URL = "https://dummyjson.com/";
    private static NetworkManager instance;
    private Retrofit retrofit;

    private NetworkManager() {
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public static NetworkManager getInstance() {
        if (instance == null) {
            instance = new NetworkManager();
        }
        return instance;
    }

    public ProductApiService getProductApiService() {
        return retrofit.create(ProductApiService.class);
    }
} 