package com.example.tradeflow.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tradeflow.model.ProductModel
import com.example.tradeflow.repository.ProductRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProductViewModel(private val repo: ProductRepo) : ViewModel() {

    /* -------------------- STATE -------------------- */
    private val _product = MutableStateFlow<ProductModel?>(null)
    val product: StateFlow<ProductModel?> = _product.asStateFlow()

    private val _allProducts = MutableStateFlow<List<ProductModel>>(emptyList())
    val allProducts: StateFlow<List<ProductModel>> = _allProducts.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    /* -------------------- CRUD -------------------- */
    fun addProduct(model: ProductModel, callback: (Boolean, String) -> Unit) {
        repo.addProduct(model, callback)
    }

    fun updateProduct(model: ProductModel, callback: (Boolean, String) -> Unit) {
        repo.updateProduct(model, callback)
    }

    fun deleteProduct(productID: String, callback: (Boolean, String) -> Unit) {
        repo.deleteProduct(productID, callback)
    }

    /* -------------------- FETCH -------------------- */
    fun getAllProduct() {
        viewModelScope.launch {
            _loading.value = true
            repo.getAllProduct { success, _, data ->
                _loading.value = false
                if (success) {
                    _allProducts.value = data ?: emptyList()
                }
            }
        }
    }

    fun getProductById(productID: String) {
        viewModelScope.launch {
            repo.getProductById(productID) { success, _, data ->
                _product.value = if (success) data else null
            }
        }
    }

    fun getProductByCategory(categoryId: String) {
        viewModelScope.launch {
            _loading.value = true
            repo.getProductByCategory(categoryId) { success, _, data ->
                _loading.value = false
                _allProducts.value = data ?: emptyList()
            }
        }
    }

    fun getProductsByOwner(ownerId: String) {
        viewModelScope.launch {
            _loading.value = true
            repo.getProductsByOwner(ownerId) { success, _, data ->
                _loading.value = false
                _allProducts.value = data ?: emptyList()
            }
        }
    }

    fun getProductsByType(type: String) {
        viewModelScope.launch {
            _loading.value = true
            repo.getProductsByType(type) { success, _, data ->
                _loading.value = false
                _allProducts.value = data ?: emptyList()
            }
        }
    }

    /* -------------------- ADMIN: LIST / UNLIST -------------------- */
    fun listProduct(
        productId: String,
        isListed: Boolean,
        callback: (Boolean, String) -> Unit
    ) {
        repo.listProduct(productId, isListed) { success, message ->
            if (success) {
                viewModelScope.launch {
                    _allProducts.value = _allProducts.value.map { product ->
                        if (product.productId == productId) {
                            product.copy(isListed = isListed)
                        } else product
                    }
                }
            }
            callback(success, message)
        }
    }

    /* -------------------- IMAGE UPLOAD (USER PART) -------------------- */
    fun uploadImage(
        context: Context,
        uri: Uri,
        callback: (String?) -> Unit
    ) {
        repo.uploadImage(context, uri, callback)
    }

    fun uploadMultipleImages(
        context: Context,
        images: List<Uri?>,
        callback: (List<String>) -> Unit
    ) {
        val results = MutableList(images.size) { "" }
        var completed = 0

        val validImages = images.mapIndexedNotNull { index, uri ->
            uri?.let { index to it }
        }

        if (validImages.isEmpty()) {
            callback(results)
            return
        }

        validImages.forEach { (index, uri) ->
            repo.uploadImage(context, uri) { url ->
                results[index] = url ?: ""
                completed++
                if (completed == validImages.size) {
                    callback(results)
                }
            }
        }
    }
}