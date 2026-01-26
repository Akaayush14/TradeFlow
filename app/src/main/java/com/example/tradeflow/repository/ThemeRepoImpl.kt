// ThemeRepositoryImpl.kt
package com.example.tradeflow.repository

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError

class ThemeRepoImpl : ThemeRepo {
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val themeRef: DatabaseReference = database.getReference("UserThemes")

    override fun saveTheme(userId: String, themeMode: String, callback: (Boolean, String) -> Unit) {
        val themeData = mapOf(
            "themeMode" to themeMode,
            "lastUpdated" to System.currentTimeMillis()
        )

        themeRef.child(userId).setValue(themeData)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, "Theme saved successfully")
                } else {
                    callback(false, "Failed to save theme: ${task.exception?.message}")
                }
            }
    }

    override fun getTheme(userId: String, callback: (Boolean, String, String?) -> Unit) {
        themeRef.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val themeMode = snapshot.child("themeMode").getValue(String::class.java)
                    callback(true, "Theme fetched", themeMode ?: "system")
                } else {
                    callback(true, "No theme found, using default", "system")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message, null)
            }
        })
    }
}