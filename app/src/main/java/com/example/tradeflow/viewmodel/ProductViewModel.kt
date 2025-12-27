package com.example.tradeflow.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tradeflow.model.ProductModel
import com.example.tradeflow.repository.ProductRepo

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

    private val _products = MutableLiveData<ProductModel?>()
    val products : MutableLiveData<ProductModel?> get() = _products

    private val _allProducts = MutableLiveData<List<ProductModel>?>()
    val allProducts : MutableLiveData<List<ProductModel>?> get() = _allProducts

    private val _loading = MutableLiveData<Boolean>()
    val loading : MutableLiveData<Boolean> get() = _loading


    fun getAllProduct(){
        _loading.postValue(true)
        repo.getAllProduct {
                succees,message,data->
            if(succees){
                _loading.postValue(false)
                _allProducts.postValue(data)
            }
        }
    }

    fun getProductById(productID: String){
        repo.getProductById(productID) {
                success,message,data->
            if(success){
                _products.postValue(data)
            } else {
                _products.postValue(null)
            }
        }
    }

    fun getProductByCategory(categoryId: String){
        _loading.postValue(true)
        repo.getProductByCategory(categoryId) {
                success,message,data->
            _loading.postValue(false)
            if(success){
                _allProducts.postValue(data)
            } else {
                _allProducts.postValue(emptyList())
            }
        }
    }
    
    fun getProductsByOwner(ownerId: String){
        _loading.postValue(true)
        repo.getProductsByOwner(ownerId) {
                success,message,data->
            _loading.postValue(false)
            if(success){
                _allProducts.postValue(data)
            } else {
                _allProducts.postValue(emptyList())
            }
        }
    }
    
    fun getProductsByType(type: String){
        _loading.postValue(true)
        repo.getProductsByType(type) {
                success,message,data->
            _loading.postValue(false)
            if(success){
                _allProducts.postValue(data)
            } else {
                _allProducts.postValue(emptyList())
            }
        }
    }
}