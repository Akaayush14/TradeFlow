package com.example.tradeflow.repository

import com.example.tradeflow.model.AdminModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AdminRepoImpl : AdminRepo {
    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    val ref: DatabaseReference = database.getReference("Admins")

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
}
