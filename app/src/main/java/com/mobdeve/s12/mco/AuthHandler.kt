package com.mobdeve.s12.mco

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class AuthHandler(context: Context) {

    private lateinit var auth : FirebaseAuth

    companion object {
        private var instance : AuthHandler? = null

        @Synchronized
        fun getInstance(context: Context): AuthHandler? {
            if(instance == null) {
                instance = AuthHandler(context.applicationContext)
            }

            return instance
        }
    }

    suspend fun createAccount(email: String, password: String, activity: Activity) : String {
        auth = Firebase.auth
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val userId = result.user?.uid

            Log.d("AuthHandler", "Successfully created account with userId = $userId")
            userId!!
        } catch (e: Exception) {
            Log.w("AuthHandler", "Error adding user to Firebase Authentication.", e)
            ""
        }
    }
}