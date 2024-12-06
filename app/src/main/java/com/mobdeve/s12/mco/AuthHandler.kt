package com.mobdeve.s12.mco

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class AuthHandler(context: Context) {
    private val appContext: Context = context.applicationContext
    private val auth = Firebase.auth

    companion object {
        @Volatile
        private var instance : AuthHandler? = null

        @Synchronized
        fun getInstance(context: Context): AuthHandler {
            return instance ?: synchronized(this) {
                // double-checked locking
                instance ?: AuthHandler(context).also { instance = it }
            }
        }
    }

    fun getUserUid(): String? {
        return auth.currentUser?.uid
    }

    fun getUserFullName(): String? {
        return auth.currentUser?.displayName
    }

    fun getUserEmail(): String? {
        return auth.currentUser?.email
    }

    suspend fun createAccount(email: String, password: String, activity: Activity) : String {
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

    suspend fun loginAccount(email: String, password: String) : String {
        return try {
            val user = auth.signInWithEmailAndPassword(email, password).await()
            Log.d("AuthHandler", "User with id = ${user.user?.email} successfully logged in.")
            "Success"
        } catch(e: FirebaseAuthInvalidUserException) {
            Log.w("AuthHandler", "User not found!", e)
            "User not found"
        } catch(e: FirebaseAuthInvalidCredentialsException) {
            Log.w("AuthHandler", "Invalid credentials!", e)
            "Invalid credentials"
        } catch(e: Exception) {
            Log.w("AuthHandler", "Error logging in to Firebase Auth")
            ""
        }
    }

    fun sendPwResetEmail(email: String): Task<Void> {
        return auth.sendPasswordResetEmail(email)
    }

    fun authSignInWithCredential(authCredential: AuthCredential): Task<AuthResult> {
        return auth.signInWithCredential(authCredential)
    }

    fun logoutAccount() {
        Log.d("AuthHandler", "Successfully logged out user.")
        this.auth.signOut()
    }

    fun getCurrentUser() : FirebaseUser? {
        return this.auth.currentUser
    }
}