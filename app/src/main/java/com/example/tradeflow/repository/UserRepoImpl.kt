package com.example.tradeflow.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.example.tradeflow.model.UserModel
import kotlin.collections.toMap

class UserRepoImpl: UserRepo{
    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    //table haru ma kaam garnu paro vane ref ma garnu paryo
    val ref: DatabaseReference = database.getReference("Users")

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

    override fun addUserToDatabase(
        userId: String,
        model: UserModel,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(userId).setValue(model).addOnCompleteListener {
            if (it.isSuccessful){
                callback(true, "Registration successful")
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

    override fun getUserById(
        userId: String,
        callback: (Boolean, String, UserModel?) -> Unit
    ) {
        ref.child(userId).addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val user=snapshot.getValue(UserModel::class.java)
                    if (user != null){
                        callback(true, "Profile Fetched ",user)
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                callback(false,error.message,null)
            }
        })
    }

    override fun getAllUser(callback: (Boolean, String, List<UserModel>?) -> Unit) {
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    var allUsers = mutableListOf<UserModel>()
                    for (data in snapshot.children){
                        var user = data.getValue(UserModel::class.java)
                        if(user!=null){
                            allUsers.add(user)
                        }
                    }
                    callback(true, "User fetched", allUsers)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }
}