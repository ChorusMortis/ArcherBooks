package com.mobdeve.s12.mco

import android.content.Context
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

class FirestoreHandler(context: Context?) {

    private val usersCollection = "users"
    private val booksCollection = "books"
    private val transactionsCollection = "transactions"

    private val EMAIL_FIELD = "emailAddress"
    private val FIRST_NAME_FIELD = "firstName"
    private val LAST_NAME_FIELD = "lastName"
    private val SIGNUP_METHOD_FIELD = "signUpMethod"
    private val USER_ID_FIELD = "userId"

    private val database = Firebase.firestore

    companion object {
        private var instance : FirestoreHandler? = null

        @Synchronized
        fun getInstance(context: Context): FirestoreHandler? {
            if(instance == null) {
                instance = FirestoreHandler(context.applicationContext)
            }

            return instance
        }
    }

    suspend fun doesUserExist(emailAdd: String) : Boolean {
        return try {
            val result = database.collection(usersCollection)
                .whereEqualTo("emailAddress", emailAdd)
                .get()
                .await()

            Log.d("FirestoreHandler", "Returned doesUserExist() = ${result.documents.isNotEmpty()}")
            result.documents.isNotEmpty()
        } catch (e: Exception) {
            Log.w("FirestoreHandler", "Error getting documents.", e)
            false
        }
    }

    fun createUser(user: UserModel) {
        database.collection(usersCollection)
            .document(user.userId)
            .set(user)
            .addOnSuccessListener {
                Log.d("FirestoreHandler", "Successfully created user with ID ${user.userId}")
            }.addOnFailureListener {
                Log.w("FirestoreHandler", "Error saving new user.")
            }
    }

    suspend fun getUserByEmail(email: String): UserModel? {
        return try {
            val result = database.collection(usersCollection)
                .whereEqualTo(EMAIL_FIELD, email)
                .limit(1)
                .get()
                .await()

            if (!result.isEmpty) {
                result.documents.first().toObject(UserModel::class.java)
            } else {
                // none found
                Log.w("getUserFromEmail", "No user found with email $email")
                null
            }
        } catch (e: Exception) {
            Log.e("getUserFromEmail", e.toString())
            null
        }
    }
}