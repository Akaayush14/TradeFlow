package com.example.tradeflow.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tradeflow.model.ProductModel
import com.example.tradeflow.repository.ProductRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProductViewModel(val repo: ProductRepo) : ViewModel() {
    fun addProduct(model: ProductModel, callback:(Boolean, String)->Unit){
        repo.addProduct(model,callback)
    }

    fun updateProduct(model: ProductModel,callback: (Boolean, String) -> Unit){
        repo.updateProduct(model,callback)
    }

    fun deleteProduct(productID:String,callback: (Boolean, String) -> Unit){
        repo.deleteProduct(productID,callback)
    }

    private val _products = MutableStateFlow<ProductModel?>(null)
    val products : StateFlow<ProductModel?> = _products.asStateFlow()

    private val _allProducts = MutableStateFlow<List<ProductModel>?>(null)
    val allProducts : StateFlow<List<ProductModel>?> = _allProducts.asStateFlow()

    private val _loading = MutableStateFlow<Boolean>(false)
    val loading : StateFlow<Boolean> = _loading.asStateFlow()


    fun getAllProduct(){
        _loading.value = true
        repo.getAllProduct { success, message, data ->
            viewModelScope.launch {
                if(success){
                    _loading.value = false
                    // Create a new list instance to ensure StateFlow detects the change
                    _allProducts.value = ArrayList(data ?: emptyList())
                } else {
                    _loading.value = false
                }
            }
        }
    }

    fun getProductById(productID: String){
        viewModelScope.launch {
            repo.getProductById(productID) { success, message, data ->
                if(success){
                    _products.value = data
                } else {
                    _products.value = null
                }
            }
        }
    }

    fun getProductByCategory(categoryId: String){
        viewModelScope.launch {
            _loading.value = true
            repo.getProductByCategory(categoryId) { success, message, data ->
                _loading.value = false
                if(success){
                    _allProducts.value = data
                } else {
                    _allProducts.value = emptyList()
                }
            }
        }
    }

    fun getProductsByOwner(ownerId: String){
        viewModelScope.launch {
            _loading.value = true
            repo.getProductsByOwner(ownerId) { success, message, data ->
                _loading.value = false
                if(success){
                    _allProducts.value = data
                } else {
                    _allProducts.value = emptyList()
                }
            }
        }
    }

    fun getProductsByType(type: String){
        viewModelScope.launch {
            _loading.value = true
            repo.getProductsByType(type) { success, message, data ->
                _loading.value = false
                if(success){
                    _allProducts.value = data
                } else {
                    _allProducts.value = emptyList()
                }
            }
        }
    }

    fun listProduct(
        productId: String,
        isListed: Boolean,
        callback: (Boolean, String) -> Unit
    ) {
        repo.listProduct(productId, isListed) { success, message ->
            if (success) {
                // Manually update the StateFlow immediately for instant UI feedback
                viewModelScope.launch {
                    val currentList = _allProducts.value
                    if (currentList != null && currentList.isNotEmpty()) {
                        // Create a new list with updated product - ensure we only update the matching productId
                        val updatedList = currentList.mapIndexed { index, product ->
                            if (product.productId == productId) {
                                // Only update the product with matching productId
                                ProductModel(
                                    productId = product.productId,
                                    name = product.name,
                                    price = product.price,
                                    imageUrl = product.imageUrl,
                                    category = product.category,
                                    location = product.location,
                                    description = product.description,
                                    type = product.type,
                                    ownerId = product.ownerId,
                                    createdAt = product.createdAt,
                                    isListed = isListed  // Update only this specific product
                                )
                            } else {
                                // Keep all other products exactly as they are
                                product
                            }
                        }
                        // Assign new list to trigger StateFlow update
                        _allProducts.value = updatedList
                    } else {
                        // If list is null or empty, refresh from database
                        getAllProduct()
                    }
                }
            }
            callback(success, message)
        }
    }
}