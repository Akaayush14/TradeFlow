package com.example.tradeflow.repository

import android.content.Context
import android.net.Uri
import com.example.tradeflow.model.ProductModel

interface ProductRepo {

    fun addProduct(
        model: ProductModel,
        callback: (Boolean, String) -> Unit
    )
    fun updateProduct(
        model: ProductModel,
        callback: (Boolean, String) -> Unit
    )
    fun deleteProduct(
        productID: String,
        callback: (Boolean, String) -> Unit
    )
    fun getAllProduct(
        callback: (Boolean, String, List<ProductModel>?) -> Unit
    )
    fun getProductById(
        productID: String,
        callback: (Boolean, String, ProductModel?) -> Unit
    )
    fun getProductByCategory(
        categoryID: String,
        callback: (Boolean, String, List<ProductModel>?) -> Unit
    )
    fun getProductsByOwner(
        ownerId: String,
        callback: (Boolean, String, List<ProductModel>?) -> Unit
    )
    fun getProductsByType(
        type: String,
        callback: (Boolean, String, List<ProductModel>?) -> Unit
    )
    fun uploadImage(context: Context, uri: Uri, callback: (String?) -> Unit)
    fun getFileNameFromUri(context: Context, uri: Uri): String?
}