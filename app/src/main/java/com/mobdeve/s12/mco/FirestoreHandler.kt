package com.mobdeve.s12.mco

import android.content.Context
import android.util.Log
import com.google.android.gms.auth.api.Auth
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.auth.User
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

    private val BOOK_ID_FIELD = "bookId"
    private val TRANSACTION_DATE_FIELD = "transactionDate"

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

    fun createTransaction(bookId: String, transactionDate: Timestamp, expectedPickupDate: Timestamp, expectedReturnDate: Timestamp, context: Context) {
        val authHandler = AuthHandler.getInstance(context)
        val currentUserId = authHandler?.getCurrentUser()?.uid
        Log.d("FirestoreHandler", "User $currentUserId is trying to create a new transaction.")

        val newTransaction = hashMapOf(
            "bookId" to bookId,
            "user" to database.collection(usersCollection).document(currentUserId!!),
            "transactionDate" to transactionDate,
            "expectedPickupDate" to expectedPickupDate,
            "expectedReturnDate" to expectedReturnDate,
            "actualPickupDate" to null,
            "actualReturnDate" to null,
            "canceledDate" to null,
            "status" to TransactionModel.Status.FOR_PICKUP.toString()
        )

        database.collection(transactionsCollection).add(newTransaction)
    }

    suspend fun getLatestTransaction(bookId: String): TransactionModel? {
        return try {
            val result = database.collection(transactionsCollection)
                .whereEqualTo(BOOK_ID_FIELD, bookId)
                .orderBy(TRANSACTION_DATE_FIELD, Query.Direction.DESCENDING)
                .get()
                .await()

            if (!result.isEmpty) {
                Log.d("FirestoreHandler", "Successfully returned the latest transaction of the book!")
                val googleBooksAPIHandler = GoogleBooksAPIHandler()
                val book = googleBooksAPIHandler.getBook(bookId)

                val transaction = result.first().data
                val userRef = transaction["user"] as DocumentReference
                val user = userRef.get().await().toObject(UserModel::class.java)

                Log.d("FirestoreHandler", "Book: $book")
                Log.d("FirestoreHandler", "User: $user")

                if(book != null && user != null) {
                    val transactionObject = TransactionModel(
                        transaction["id"].toString(),
                        book,
                        user,
                        transaction["transactionDate"] as Timestamp,
                        transaction["expectedPickupDate"] as Timestamp,
                        transaction["expectedReturnDate"] as Timestamp,
                        transaction["actualPickupDate"] as Timestamp?,
                        transaction["actualReturnDate"] as Timestamp?,
                        transaction["canceledDate"] as Timestamp?,
                        TransactionModel.Status.valueOf(transaction["status"].toString())
                    )
                    transactionObject
                } else {
                    null
                }


            } else {
                // none found
                Log.w("FirestoreHandler", "No transaction found of the book with ID: $bookId")
                null
            }
        } catch (e: Exception) {
            Log.e("FirestoreHandler", "Search for latest transaction exception: $e")
            null
        }
    }
}