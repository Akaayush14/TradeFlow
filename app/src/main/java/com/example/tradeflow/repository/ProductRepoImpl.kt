package com.example.tradeflow.repository

import android.content.Context
import android.database.Cursor
import android.util.Log
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
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.io.InputStream
import java.util.UUID
import java.util.concurrent.Executors
import kotlin.collections.toMap


class ProductRepoImpl: ProductRepo {
    val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    val ref: DatabaseReference = database.getReference("products")
    val storageRef = FirebaseStorage.getInstance().reference
    private val cloudinary = Cloudinary(
        mapOf(
            "cloud_name" to "dpi7b9iam",
            "api_key" to "561879326562495",
            "api_secret" to "iteXJaLRqFgpuMwmVcw0gw9fjgE"
        )
    )

    override suspend fun uploadImages(context: Context, uris: List<Uri?>): List<String> {
        val tag = "TF_IMAGE_UPLOAD"
        val uploadedUrls = MutableList(uris.size) { "" }
        for ((index, uri) in uris.withIndex()) {
            if (uri != null) {
                Log.d(tag, "Starting upload for index=$index uri=$uri")
                val fileName = UUID.randomUUID().toString() + ".jpg"
                val imageRef = storageRef.child("product_images/$fileName")
                try {
                    imageRef.putFile(uri).await() // Wait for upload
                    val downloadUrl = imageRef.downloadUrl.await() // Wait for URL
                    uploadedUrls[index] = downloadUrl.toString()
                    Log.d(tag, "Upload success index=$index url=${uploadedUrls[index]}")
                } catch (e: Exception) {
                    Log.e(tag, "Upload failed index=$index uri=$uri error=${e.message}")
                    e.printStackTrace()
                    // Leave as empty string on error
                }
            }
        }
        Log.d(tag, "All upload results: $uploadedUrls")
        return uploadedUrls
    }

    override fun addProduct(
        model: ProductModel,
        callback: (Boolean, String) -> Unit
    ) {
        var productId = ref.push().key.toString()
        model.productId = productId
        val tag = "TF_FIRESTORE_SAVE"
        Log.d(tag, "Saving productId=$productId with imageUrl=${model.imageUrl} imageUrls=${model.imageUrls}")
        ref.child(productId).setValue(model).addOnCompleteListener {
            if (it.isSuccessful) {
                Log.d(tag, "Product saved successfully productId=$productId")
                callback(true, "Product added successfully")
            } else {
                Log.e(tag, "Product save failed productId=$productId error=${it.exception?.message}")
                callback(false, "${it.exception?.message}")
            }
        }
    }

    override fun updateProduct(
        model: ProductModel,
        callback: (Boolean, String) -> Unit
    ) {
        val tag = "TF_FIRESTORE_SAVE"
        Log.d(tag, "Updating productId=${model.productId} with imageUrl=${model.imageUrl} imageUrls=${model.imageUrls}")
        ref.child(model.productId).updateChildren(model.toMap()).addOnCompleteListener {
            if (it.isSuccessful) {
                Log.d(tag, "Product updated successfully productId=${model.productId}")
                callback(true, "Product updated successfully")
            } else {
                Log.e(tag, "Product update failed productId=${model.productId} error=${it.exception?.message}")
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
                    val tag = "TF_FIRESTORE_FETCH"
                    Log.d(tag, "Fetching all products count=${snapshot.childrenCount}")
                    val allProducts = mutableListOf<ProductModel>()

                    for (data in snapshot.children) {
                        val product = data.getValue(ProductModel::class.java)

                        if (product != null) {
                            // Handle isListed safely
                            product.isListed = if (data.hasChild("isListed")) {
                                data.child("isListed").getValue(Boolean::class.java) ?: true
                            } else {
                                true // default if field does not exist
                            }

                            allProducts.add(product)
                            Log.d(tag, "Fetched productId=${product.productId} imageUrl=${product.imageUrl} imageUrls=${product.imageUrls}")
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
                        Log.d("TF_FIRESTORE_FETCH", "Fetched productId=${data.productId} imageUrl=${data.imageUrl} imageUrls=${data.imageUrls}")
                        callback(true, "product fetched", data)
                    } else {
                        Log.e("TF_FIRESTORE_FETCH", "Product data null for productId=$productID")
                        callback(false, "Product data is null", null)
                    }
                } else {
                    Log.e("TF_FIRESTORE_FETCH", "Product not found productId=$productID")
                    callback(false, "Product not found", null)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("TF_FIRESTORE_FETCH", "Fetch cancelled productId=$productID error=${error.message}")
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
    override fun uploadImage(context: Context, imageUri: Uri, callback: (String?) -> Unit) {
        val executor = Executors.newSingleThreadExecutor()
        executor.execute {
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)
                var fileName = getFileNameFromUri(context, imageUri)

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

    override fun getFileNameFromUri(context: Context, uri: Uri): String? {
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


    override fun listProduct(
        productId: String,
        isListed: Boolean,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(productId).child("isListed").setValue(isListed).addOnCompleteListener {
            if(it.isSuccessful){
                val message = if(isListed) "Product listed successfully" else "Product unlisted successfully"
                callback(true, message)
            }else{
                callback(false, "${it.exception?.message}")
            }
        }
    }
}


