package com.praneet.neo.viewmodel;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.LiveData;
import com.praneet.neo.model.Product;
import com.praneet.neo.repository.ProductRepository;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomeViewModel extends ViewModel {
    private final ProductRepository repository;
    private MutableLiveData<HomeUiState> uiState = new MutableLiveData<>(new HomeUiState.Loading());
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    public HomeViewModel(ProductRepository repository) {
        this.repository = repository;
        fetchProducts();
    }

    public LiveData<HomeUiState> getUiState() {
        return uiState;
    }

    public void fetchProducts() {
        uiState.setValue(new HomeUiState.Loading());
        executor.execute(() -> {
            try {
                List<Product> products = repository.getProducts();
                uiState.postValue(new HomeUiState.Success(products));
            } catch (Exception e) {
                String errorMessage = e.getLocalizedMessage();
                if (errorMessage == null) {
                    errorMessage = "Unknown error";
                }
                uiState.postValue(new HomeUiState.Error(errorMessage));
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown();
    }
} 