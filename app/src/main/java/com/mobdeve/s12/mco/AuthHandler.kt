package com.mobdeve.s12.mco

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

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

    fun createAccount(email: String, password: String, activity: Activity) {
        auth = Firebase.auth
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(activity) { task ->
                if(task.isSuccessful) {
                    Log.d("AuthHandler", "Successfully added user to Firebase Authentication")
                } else {
                    Log.d("AuthHandler", "Error adding user to Firebase Authentication")
                }
            }
    }

}