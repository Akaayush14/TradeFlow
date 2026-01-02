package com.example.tradeflow.repository

import android.content.Context
import android.database.Cursor
import android.os.Handler
import android.os.Looper
import android.provider.OpenableColumns
import android.net.Uri
import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import com.example.tradeflow.model.ProductModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.io.InputStream
import java.util.concurrent.Executors
import kotlin.collections.toMap


class ProductRepoImpl: ProductRepo {
    val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    val ref: DatabaseReference = database.getReference("products")
    private val cloudinary = Cloudinary(
        mapOf(
            "cloud_name" to "dpi7b9iam",
            "api_key" to "561879326562495",
            "api_secret" to "iteXJaLRqFgpuMwmVcw0gw9fjgE"
        )
    )

    override fun addProduct(
        model: ProductModel,
        callback: (Boolean, String) -> Unit
    ) {
        var productId = ref.push().key.toString()
        model.productId = productId

        ref.child(productId).setValue(model).addOnCompleteListener {
            if (it.isSuccessful) {
                callback(true, "Product added successfully")
            } else {
                callback(false, "${it.exception?.message}")
            }
        }
    }

    override fun updateProduct(
        model: ProductModel,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(model.productId).updateChildren(model.toMap()).addOnCompleteListener {
            if (it.isSuccessful) {
                callback(true, "Product updated successfully")
            } else {
                callback(false, "${it.exception?.message}")
            }
        }
    }

    override fun deleteProduct(
        productID: String,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(productID).removeValue().addOnCompleteListener {
            if (it.isSuccessful) {
                callback(true, "Product deleted successfully")
            } else {
                callback(false, "${it.exception?.message}")
            }
        }
    }

    override fun getAllProduct(callback: (Boolean, String, List<ProductModel>?) -> Unit) {
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val allProducts = mutableListOf<ProductModel>()
                    for (data in snapshot.children) {
                        var product = data.getValue(ProductModel::class.java)
                        if (product != null) {
                            allProducts.add(product)
                        }
                    }
                    callback(true, "fetched", allProducts)
                } else {
                    callback(true, "No products found", emptyList())
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message, null)
            }
        })
    }

    override fun getProductById(
        productID: String,
        callback: (Boolean, String, ProductModel?) -> Unit
    ) {
        ref.child(productID).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    var data = snapshot.getValue(ProductModel::class.java)
                    if (data != null) {
                        callback(true, "product fetched", data)
                    } else {
                        callback(false, "Product data is null", null)
                    }
                } else {
                    callback(false, "Product not found", null)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message, null)
            }
        })
    }

    override fun getProductByCategory(
        categoryID: String,
        callback: (Boolean, String, List<ProductModel>?) -> Unit
    ) {
        ref.orderByChild("category").equalTo(categoryID)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val allProducts = mutableListOf<ProductModel>()
                        for (data in snapshot.children) {
                            var product = data.getValue(ProductModel::class.java)
                            if (product != null) {
                                allProducts.add(product)
                            }
                        }
                        callback(true, "fetched", allProducts)
                    } else {
                        callback(true, "No products found in this category", emptyList())
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false, error.message, null)
                }
            })
    }

    override fun getProductsByOwner(
        ownerId: String,
        callback: (Boolean, String, List<ProductModel>?) -> Unit
    ) {
        ref.orderByChild("ownerId").equalTo(ownerId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val allProducts = mutableListOf<ProductModel>()
                        for (data in snapshot.children) {
                            var product = data.getValue(ProductModel::class.java)
                            if (product != null) {
                                allProducts.add(product)
                            }
                        }
                        callback(true, "fetched", allProducts)
                    } else {
                        callback(true, "No products found", emptyList())
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false, error.message, null)
                }
            })
    }

    override fun getProductsByType(
        type: String,
        callback: (Boolean, String, List<ProductModel>?) -> Unit
    ) {
        ref.orderByChild("type").equalTo(type).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val allProducts = mutableListOf<ProductModel>()
                    for (data in snapshot.children) {
                        var product = data.getValue(ProductModel::class.java)
                        if (product != null) {
                            allProducts.add(product)
                        }
                    }
                    callback(true, "fetched", allProducts)
                } else {
                    callback(true, "No products found", emptyList())
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message, null)
            }
        })
    }

    override fun uploadImage(context: Context, uri: Uri, callback: (String?) -> Unit) {
        val executor = Executors.newSingleThreadExecutor()
        executor.execute {
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                var fileName = getFileNameFromUri(context, uri)

                fileName = fileName?.substringBeforeLast(".") ?: "uploaded_image"

                val response = cloudinary.uploader().upload(
                    inputStream, ObjectUtils.asMap(
                        "public_id", fileName,
                        "resource_type", "image"
                    )
                )

                var imageUrl = response["url"] as String?

                imageUrl = imageUrl?.replace("http://", "https://")

                Handler(Looper.getMainLooper()).post {
                    callback(imageUrl)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Handler(Looper.getMainLooper()).post {
                    callback(null)
                }
            }
        }
    }


    override fun getFileNameFromUri(
        context: Context,
        uri: Uri
    ): String? {
        var fileName: String? = null
        val cursor: Cursor? = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    fileName = it.getString(nameIndex)
                }
            }
        }
        return fileName
    }
}
