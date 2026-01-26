package com.example.tradeflow.repository

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.OpenableColumns
import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import com.example.tradeflow.model.AdminModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.io.InputStream
import java.util.concurrent.Executors

class AdminRepoImpl : AdminRepo {
    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    val ref: DatabaseReference = database.getReference("Admins")
    
    private val cloudinary = Cloudinary(
        mapOf(
            "cloud_name" to "dpi7b9iam",
            "api_key" to "561879326562495",
            "api_secret" to "iteXJaLRqFgpuMwmVcw0gw9fjgE"
        )
    )

    override fun login(
        email: String,
        password: String,
        callback: (Boolean, String) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if(it.isSuccessful){
                    callback(true, "login successful")
                }else{
                    callback(false, "${it.exception?.message}")
                }
            }
    }

    override fun register(
        email: String,
        password: String,
        phone: String,
        callback: (Boolean, String, String) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful){
                    callback(true, "Registration success",
                        "${auth.currentUser?.uid}")
                }else{
                    callback(false, "${it.exception?.message}", "")
                }
            }
    }

    override fun addAdminToDatabase(
        userId: String,
        model: AdminModel,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(userId).setValue(model).addOnCompleteListener {
            if (it.isSuccessful){
                callback(true, "Admin registered successfully")
            }else{
                callback(true, "${it.exception?.message}")
            }
        }
    }

    override fun forgetPassword(
        email: String,
        callback: (Boolean, String) -> Unit
    ) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    callback(true, "Link sent to $email")
                } else {
                    callback(false, "${it.exception?.message}")
                }
            }
    }

    override fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    override fun getAdminById(
        userId: String,
        callback: (Boolean, String, AdminModel?) -> Unit
    ) {
        ref.child(userId).addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val admin = snapshot.getValue(AdminModel::class.java)
                    if (admin != null){
                        callback(true, "Profile Fetched ", admin)
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                callback(false,error.message,null)
            }
        })
    }

    override fun getAllAdmins(callback: (Boolean, String, List<AdminModel>?) -> Unit) {
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    if(snapshot.exists()){
                        var allAdmins = mutableListOf<AdminModel>()
                        for (data in snapshot.children){
                            try {
                                var admin = data.getValue(AdminModel::class.java)
                                if(admin != null){
                                    val userId = data.key ?: ""
                                    if (userId.isEmpty()) continue
                                    
                                    admin.userId = userId
                                    admin.name = data.child("name").getValue(String::class.java) ?: ""
                                    admin.email = data.child("email").getValue(String::class.java) ?: ""
                                    admin.phone = data.child("phone").getValue(String::class.java) ?: ""
                                    admin.dateOfBirth = data.child("dateOfBirth").getValue(String::class.java) ?: ""
                                    admin.imageUrl = data.child("imageUrl").getValue(String::class.java) ?: ""
                                    
                                    if (data.hasChild("isBlocked")) {
                                        val isBlockedValue = data.child("isBlocked").getValue(Boolean::class.java)
                                        admin.isBlocked = isBlockedValue ?: false
                                    } else {
                                        admin.isBlocked = false
                                    }
                                    
                                    if (data.hasChild("isRestricted")) {
                                        val isRestrictedValue = data.child("isRestricted").getValue(Boolean::class.java)
                                        admin.isRestricted = isRestrictedValue ?: false
                                    } else {
                                        admin.isRestricted = false
                                    }
                                    
                                    if (admin.userId.isNotEmpty()) {
                                        allAdmins.add(admin)
                                    }
                                }
                            } catch (e: Exception) {
                                continue
                            }
                        }
                        callback(true, "Admins fetched", allAdmins)
                    } else {
                        callback(true, "No admins found", emptyList())
                    }
                } catch (e: Exception) {
                    callback(false, "Error: ${e.message}", null)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message ?: "Unknown error", null)
            }
        })
    }

    override fun deleteAdmin(
        userId: String,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(userId).removeValue().addOnCompleteListener {
            if(it.isSuccessful){
                callback(true, "Admin deleted successfully")
            }else{
                callback(false, "${it.exception?.message}")
            }
        }
    }

    override fun updateAdminStatus(
        userId: String,
        isBlocked: Boolean,
        isRestricted: Boolean,
        callback: (Boolean, String) -> Unit
    ) {
        val updates = mapOf(
            "isBlocked" to isBlocked,
            "isRestricted" to isRestricted
        )
        ref.child(userId).updateChildren(updates).addOnCompleteListener {
            if (it.isSuccessful) {
                callback(true, "Admin status updated")
            } else {
                callback(false, "${it.exception?.message}")
            }
        }
    }

    override fun updateAdmin(
        userId: String,
        data: Map<String, Any>,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(userId).updateChildren(data).addOnCompleteListener {
            if (it.isSuccessful) {
                callback(true, "Profile updated successfully")
            } else {
                callback(false, it.exception?.message ?: "Error updating profile")
            }
        }
    }

    override fun uploadImage(context: Context, imageUri: Uri, callback: (String?) -> Unit) {
        val executor = Executors.newSingleThreadExecutor()
        executor.execute {
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)
                var fileName = getFileNameFromUri(context, imageUri)

                fileName = fileName?.substringBeforeLast(".") ?: "admin_image"

                val response = cloudinary.uploader().upload(
                    inputStream, ObjectUtils.asMap(
                        "public_id", "admin_images/$fileName", // Use a separate folder for admin images
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

    override fun changePassword(
        currentPassword: String,
        newPassword: String,
        callback: (Boolean, String) -> Unit
    ) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            callback(false, "No user is logged in")
            return
        }

        val email = currentUser.email
        if (email.isNullOrEmpty()) {
            callback(false, "User email not found")
            return
        }

        val credential = com.google.firebase.auth.EmailAuthProvider.getCredential(email, currentPassword)
        currentUser.reauthenticate(credential)
            .addOnCompleteListener { reauthTask ->
                if (reauthTask.isSuccessful) {
                    // Update password
                    currentUser.updatePassword(newPassword)
                        .addOnCompleteListener { updateTask ->
                            if (updateTask.isSuccessful) {
                                callback(true, "Password changed successfully")
                            } else {
                                callback(false, updateTask.exception?.message ?: "Failed to update password")
                            }
                        }
                } else {
                    callback(false, reauthTask.exception?.message ?: "Current password is incorrect")
                }
            }
    }
}
