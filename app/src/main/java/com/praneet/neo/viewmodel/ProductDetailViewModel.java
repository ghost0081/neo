package com.praneet.neo.viewmodel;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.LiveData;
import com.praneet.neo.model.Product;

public class ProductDetailViewModel extends ViewModel {
    private MutableLiveData<Product> product = new MutableLiveData<>();

    public LiveData<Product> getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product.setValue(product);
    }
} 