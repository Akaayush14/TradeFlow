package com.example.tradeflow.repository

import com.example.tradeflow.model.ProductModel

class ProductRepoImpl: ProductRepo {
    override fun addProduct(
        model: ProductModel,
        callback: (Boolean, String) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun updateProduct(
        model: ProductModel,
        callback: (Boolean, String) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun deleteProduct(
        productID: String,
        callback: (Boolean, String) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun getAllProduct(callback: (Boolean, String, List<ProductModel>?) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun getProductById(
        productID: String,
        callback: (Boolean, String, ProductModel?) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun getProductByCategory(
        categoryID: String,
        callback: (Boolean, String, List<ProductModel>?) -> Unit
    ) {
        TODO("Not yet implemented")
    }
}