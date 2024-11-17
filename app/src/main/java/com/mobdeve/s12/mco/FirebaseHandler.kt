package com.mobdeve.s12.mco

import android.content.Context
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class FirebaseHandler(context: Context?) {

    private val USERS_COLLECTION = "users"
    private val BOOKS_COLLECTION = "books"
    private val TRANSACTIONS_COLLECTION = "transactions"

    private lateinit var database : FirebaseFirestore

    companion object {
        private var instance : FirebaseHandler? = null

        @Synchronized
        fun getInstance(context: Context): FirebaseHandler? {
            if(instance == null) {
                instance = FirebaseHandler(context.applicationContext)
            }

            return instance
        }
    }

    suspend fun doesUserExist(emailAdd: String) : Boolean {
        val database = Firebase.firestore

        return try {
            val result = database.collection(USERS_COLLECTION)
                .whereEqualTo("emailAddress", emailAdd)
                .get()
                .await()

            Log.d("FirebaseHandler", "Returned doesUserExist() = ${result.documents.isNotEmpty()}")
            result.documents.isNotEmpty()
        } catch (e: Exception) {
            Log.w("FirebaseHandler", "Error getting documents.", e)
            false
        }
    }

}