package com.mobdeve.s12.mco

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await
import java.time.ZoneId
import java.util.Calendar
import java.util.Date
import kotlin.math.exp

class FirestoreHandler private constructor(context: Context) {
    private val appContext: Context = context.applicationContext

    private val usersCollection = "users"
    private val booksCollection = "books"
    private val transactionsCollection = "transactions"

    private val EMAIL_FIELD = "emailAddress"
    private val FIRST_NAME_FIELD = "firstName"
    private val LAST_NAME_FIELD = "lastName"
    private val RECENTLY_VIEWED_FIELD = "recentlyViewed"
    private val SIGNUP_METHOD_FIELD = "signUpMethod"
    private val USER_ID_FIELD = "userId"
    private val FAVORITES_FIELD = "favorites"

    private val BOOK_FIELD = "book"
    private val USER_FIELD = "user"
    private val ACTUAL_PICKUP_DATE_FIELD = "actualPickupDate"
    private val ACTUAL_RETURN_DATE_FIELD = "actualReturnDate"
    private val CANCELED_DATE_FIELD = "canceledDate"
    private val EXPECTED_PICKUP_DATE_FIELD = "expectedPickupDate"
    private val EXPECTED_RETURN_DATE_FIELD = "expectedReturnDate"
    private val STATUS_FIELD = "status"
    private val TRANSACTION_DATE_FIELD = "transactionDate"
    private val FIRST_AUTHOR_INDEX_FIELD = "firstAuthorIndex"
    private val BOOK_TITLE_INDEX_FIELD = "bookTitleIndex"


    private val database = Firebase.firestore

    private val MAX_RV_BOOK_COUNT = 5

    companion object {
        @Volatile
        private var instance : FirestoreHandler? = null

        @Synchronized
        fun getInstance(context: Context): FirestoreHandler {
            return instance ?: synchronized(this) {
                // double-checked locking
                instance ?: FirestoreHandler(context).also { instance = it }
            }
        }
    }

    /* Users Collection */
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

    // Use this function to get the current authenticated user's UserModel object.
    suspend fun getCurrentUserModel(): UserModel? {
        val authHandler = AuthHandler.getInstance(appContext)
        val uid = authHandler.getUserUid() ?: return null
        return try {
            val userRef = database.collection(usersCollection).document(uid).get().await()
            val userData = userRef.data

            convertToUserModel(userData, userRef)
        } catch (e: Exception) {
            Log.e("FirestoreHandler", "Error getting current user model", e)
            null
        }
    }

    private suspend fun convertToUserModel(userData: Map<String, Any>?, userRef: DocumentSnapshot) : UserModel {
        // handle recently viewed book models
        val recentlyViewedRefs = userData?.get("recentlyViewed") as List<DocumentReference>
        val recentlyViewedObjs : ArrayList<BookModel> = arrayListOf()
        if(recentlyViewedRefs.isNotEmpty()) {
            recentlyViewedRefs.map { recentlyViewedRef ->
                val recentlyViewedObj = recentlyViewedRef.get().await().toObject(BookModel::class.java)
                if(recentlyViewedObj != null) {
                    recentlyViewedObjs += recentlyViewedObj
                }
            }
        }

        // handle favorite book models
        val favoritesRefs = userData["favorites"] as List<DocumentReference>
        val favoritesObjs : ArrayList<BookModel> = arrayListOf()
        if(favoritesRefs.isNotEmpty()) {
            favoritesRefs.map { favoritesRef ->
                val favoritesObj = favoritesRef.get().await().toObject(BookModel::class.java)
                if(favoritesObj != null) {
                    favoritesObjs += favoritesObj
                }
            }
        }

        return UserModel(
            userRef.id,
            userData["firstName"] as String,
            userData["lastName"] as String,
            userData["emailAddress"] as String,
            UserModel.SignUpMethod.valueOf(userData["signUpMethod"].toString()),
            recentlyViewedObjs,
            favoritesObjs
        )
    }

    suspend fun isUserAnAdmin(emailAdd: String) : Boolean {
        val result = database.collection(usersCollection)
            .whereEqualTo(EMAIL_FIELD, emailAdd)
            .limit(1)
            .get()
            .await()

        if(!result.isEmpty) {
            val userData = result.documents.first().data
            return userData?.get("admin") as Boolean
        }

        return false
    }


    // Use this function when the user is not logged in or
    // if it is unclear whether they are authenticated or not.
    suspend fun getUserByEmail(email: String): UserModel? {
        return try {
            val result = database.collection(usersCollection)
                .whereEqualTo(EMAIL_FIELD, email)
                .limit(1)
                .get()
                .await()

            if (!result.isEmpty) {
//                result.documents.first().toObject(UserModel::class.java)
                val userRef = result.documents.first()
                val userData = userRef.data
                convertToUserModel(userData, userRef)
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

    suspend fun getRecentlyViewedBooks(): ArrayList<BookModel>? {
        return getCurrentUserModel()?.recentlyViewed
    }

    suspend fun addBookToRecentlyViewed(bookId: String) {
        val authHandler = AuthHandler.getInstance(appContext)
        val uid = authHandler.getUserUid() ?: return

        createBook(bookId)
        val book = getBook(bookId) ?: return

        val rvBooks = getRecentlyViewedBooks() ?: arrayListOf()

        Log.i("FirestoreHandler", "rvBooks size = ${rvBooks.size}")
        if (rvBooks.contains(book)) {
            rvBooks.remove(book)
        } else {
            if (rvBooks.isNotEmpty() && rvBooks.size >= MAX_RV_BOOK_COUNT) {
                rvBooks.removeLast()
            }
        }
        // add book to start of list so it's always at the leftmost side of recycler view
        rvBooks.add(0, book)

        // remove extra books at end of list so it is only at most MAX_RV_BOOK_COUNT at all times
        // for future-proofing in case MAX_RV_BOOK_COUNT is modified
        if (rvBooks.size > MAX_RV_BOOK_COUNT) {
            rvBooks.subList(MAX_RV_BOOK_COUNT, rvBooks.size).clear()
        }

        val rvBookDocRefs = convertBookModelsToDocRefs(rvBooks)
        database.collection(usersCollection).document(uid)
            .update(RECENTLY_VIEWED_FIELD, rvBookDocRefs)
            .addOnSuccessListener {
                Log.d("FirestoreHandler", "Successfully updated recently viewed books list")
            }
            .addOnFailureListener {
                Log.e("FirestoreHandler", "Error updating recently viewed books list")
            }
    }

    suspend fun isBookFavorited(bookId: String): Boolean? {
        return try {
            val currentUser = getCurrentUserModel()
            val book = getBook(bookId)
            if(currentUser != null) {
                Log.d("FirestoreHandler", "Successfully found user when checking for isBookFavorited")
                currentUser.favorites.contains(book)
            } else {
                Log.w("FirestoreHandler", "Obtained current user is null when checking for isBookFavorited")
                null
            }

        } catch(e: Exception) {
            Log.e("FirestoreHandler", "Error checking if book $bookId is part of current user's favorites.", e)
            null
        }
    }

    suspend fun addToFavorites(bookId: String) {
        val authHandler = AuthHandler.getInstance(appContext)
        val currentUserId = authHandler.getUserUid()
        createBook(bookId)
        val bookRef = database.collection(booksCollection).document(bookId)

        if(currentUserId != null) {
            database.collection(usersCollection).document(currentUserId)
                .update(FAVORITES_FIELD, FieldValue.arrayUnion(bookRef))
                .addOnSuccessListener {
                    Log.d("FirestoreHandler", "Successfully added book $bookId to current user's favorites!")
                }
                .addOnFailureListener {
                    Log.e("FirestoreHandler", "Error adding book $bookId to current user's favorites")
                }
        } else {
            Log.e("FirestoreHandler", "Obtained userId from AuthHandler was null when called from addToFavorites()")
        }
    }

    fun removeFromFavorites(bookId: String) {
        val authHandler = AuthHandler.getInstance(appContext)
        val currentUserId = authHandler.getUserUid()
        val bookRef = database.collection(booksCollection).document(bookId)

        if(currentUserId != null) {
            database.collection(usersCollection).document(currentUserId)
                .update(FAVORITES_FIELD, FieldValue.arrayRemove(bookRef))
                .addOnSuccessListener {
                    Log.d("FirestoreHandler", "Successfully removed book $bookId from current user's favorites!")
                }
                .addOnFailureListener {
                    Log.e("FirestoreHandler", "Error removing book $bookId from current user's favorites")
                }
        } else {
            Log.e("FirestoreHandler", "Obtained userId from AuthHandler was null when called from removeFromFavorites()")
        }
    }

    suspend fun getAllFavorites(sortOption: FavoritesFragment.SortOption) : ArrayList<BookModel>? {
        return try {
            val authHandler = AuthHandler.getInstance(appContext)
            val uid = authHandler.getUserUid()

            val userRef = database.collection(usersCollection).document(uid!!).get().await()
            val userData = userRef.data

            if(userData != null) {
                val favoritesRefs = userData["favorites"] as List<DocumentReference>
                val favoritesObjs : ArrayList<BookModel> = arrayListOf()
                if(favoritesRefs.isNotEmpty()) {
                    favoritesRefs.map { favoritesRef ->
                        val favoritesObj = favoritesRef.get().await().toObject(BookModel::class.java)
                        if(favoritesObj != null) {
                            favoritesObjs += favoritesObj
                        }
                    }
                }

                Log.d("FirestoreHandler", "Successfully returning the favorites list (empty or not) from the backend!")
                when (sortOption) {
                    FavoritesFragment.SortOption.RECENT_FAV -> {
                        ArrayList(favoritesObjs)
                    }
                    FavoritesFragment.SortOption.NEWEST -> {
                        ArrayList(favoritesObjs.sortedByDescending { it.publishYear })
                    }
                    FavoritesFragment.SortOption.TITLE -> {
                        ArrayList(favoritesObjs.sortedBy { it.title })
                    }
                    FavoritesFragment.SortOption.AUTHOR -> {
                        ArrayList(favoritesObjs.sortedBy { it.authors[0] })
                    }
                }
            } else {
                Log.e("FirestoreHandler", "Obtained user from AuthHandler was null when called from getAllFavorites()")
                null
            }

        } catch(e: Exception) {
            Log.e("FirestoreHandler", "Error getting all favorited books from Firestore", e)
            null
        }
    }

    suspend fun getTotalFavorites() : Int {
        val authHandler = AuthHandler.getInstance(appContext)
        val uid = authHandler.getUserUid()

        val userRef = database.collection(usersCollection).document(uid!!).get().await()
        val userData = userRef.data

        var count  = 0
        if(userData != null) {
            val favoritesList = userData["favorites"] as List<DocumentReference>
            count = favoritesList.size
        }

        return count
    }

    /* Transactions Collection */

    suspend fun createTransaction(bookId: String, transactionDate: Timestamp, expectedPickupDate: Timestamp, expectedReturnDate: Timestamp): String {
        val authHandler = AuthHandler.getInstance(appContext)
        val currentUserId = authHandler.getCurrentUser()?.uid

        Log.d("FirestoreHandler", "User $currentUserId is trying to create a new transaction.")
        createBook(bookId) // should already handle if book already exists
        val book = getBook(bookId)

        val newTransaction = hashMapOf(
            "book" to database.collection(booksCollection).document(bookId),
            "user" to database.collection(usersCollection).document(currentUserId!!),
            "firstAuthorIndex" to book?.authors?.get(0), // used for sorting later
            "bookTitleIndex" to book?.title, // used for sorting later
            "transactionDate" to transactionDate,
            "expectedPickupDate" to expectedPickupDate,
            "expectedReturnDate" to expectedReturnDate,
            "actualPickupDate" to null,
            "actualReturnDate" to null,
            "canceledDate" to null,
            "status" to TransactionModel.Status.FOR_PICKUP.toString()
        )

        val docRef = database.collection(transactionsCollection).add(newTransaction).await()
        return docRef.id
    }

    suspend fun getLatestTransaction(bookId: String): TransactionModel? {
        return try {
            val bookRef = database.collection(booksCollection).document(bookId)
            val result = database.collection(transactionsCollection)
                .whereEqualTo(BOOK_FIELD, bookRef)
                .orderBy(TRANSACTION_DATE_FIELD, Query.Direction.DESCENDING)
                .get()
                .await()

            if (!result.isEmpty) {
                val book = bookRef.get().await().toObject(BookModel::class.java)
                val transaction = result.first().data
                val userRef = transaction[USER_FIELD] as DocumentReference
                val userDoc = userRef.get().await()
                val userData = userDoc.data
                val user = convertToUserModel(userData, userDoc)

                if(book != null && user != null) {
                    val transactionObject = TransactionModel(
                        result.first().id,
                        book,
                        user,
                        transaction[TRANSACTION_DATE_FIELD] as Timestamp,
                        transaction[EXPECTED_PICKUP_DATE_FIELD] as Timestamp,
                        transaction[EXPECTED_RETURN_DATE_FIELD] as Timestamp,
                        transaction[ACTUAL_PICKUP_DATE_FIELD] as Timestamp?,
                        transaction[ACTUAL_RETURN_DATE_FIELD] as Timestamp?,
                        transaction[CANCELED_DATE_FIELD] as Timestamp?,
                        TransactionModel.Status.valueOf(transaction[STATUS_FIELD].toString())
                    )
                    Log.d("FirestoreHandler", "Successfully returned the latest transaction of the book!")
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

    suspend fun getLatestTransactionId(userId: String, bookId: String): String? {
        return try {
            val userRef = database.collection(usersCollection).document(userId)
            val bookRef = database.collection(booksCollection).document(bookId)

            val transactions = database.collection(transactionsCollection)
                .whereEqualTo(USER_FIELD, userRef)
                .whereEqualTo(BOOK_FIELD, bookRef)
                .orderBy(TRANSACTION_DATE_FIELD, Query.Direction.DESCENDING)
                .get()
                .await()

            if (!transactions.isEmpty) {
                val latestTransaction = transactions.first()
                Log.d("FirestoreHandler", "Successfully found transaction with ID ${latestTransaction.id}")
                latestTransaction.id
            } else {
                Log.w("FirestoreHandler", "Transaction not found with $userId and bookId $bookId")
                null
            }
        } catch(e: Exception) {
            Log.e("FirestoreHandler", "Error finding transaction ID with userID $userId and bookId $bookId", e)
            null
        }
    }

    fun updateTransaction(transactionId: String, fieldName: String, newValue: Any) {
        database.collection(transactionsCollection).document(transactionId)
            .update(fieldName, newValue)
            .addOnSuccessListener {
                Log.d("FirestoreHandler", "Successfully updated $fieldName of transaction $transactionId to $newValue")
            }
            .addOnFailureListener{
                Log.e("FirestoreHandler", "Error updating $fieldName of transaction $transactionId")
            }
    }

    suspend fun getTransactions(increment: Long, sortOption: TransactionsFragment.SortOption,
                                filterOption: TransactionsFragment.FilterOption,
                                startAfterDoc: DocumentSnapshot?) : Pair<ArrayList<TransactionModel>, DocumentSnapshot?>? {
        try {
            val authHandler = AuthHandler.getInstance(appContext)
            val currentUserId = authHandler.getUserUid()
            val currentUserRef = database.collection(usersCollection).document(currentUserId!!)

            val sortingField = when(sortOption) {
                TransactionsFragment.SortOption.NEWEST -> TRANSACTION_DATE_FIELD
                TransactionsFragment.SortOption.AUTHOR -> FIRST_AUTHOR_INDEX_FIELD
                TransactionsFragment.SortOption.TITLE -> BOOK_TITLE_INDEX_FIELD
            }

            val order = when(sortOption) {
                TransactionsFragment.SortOption.NEWEST -> Query.Direction.DESCENDING
                TransactionsFragment.SortOption.AUTHOR -> Query.Direction.ASCENDING
                TransactionsFragment.SortOption.TITLE -> Query.Direction.ASCENDING
            }

            val querySnapshot : QuerySnapshot
            if(startAfterDoc != null) {
                if(filterOption == TransactionsFragment.FilterOption.ALL) {
                    querySnapshot = database.collection(transactionsCollection)
                        .whereEqualTo(USER_FIELD, currentUserRef)
                        .orderBy(sortingField, order)
                        .startAfter(startAfterDoc)
                        .limit(increment)
                        .get()
                        .await()
                } else {
                    querySnapshot = database.collection(transactionsCollection)
                        .whereEqualTo(USER_FIELD, currentUserRef)
                        .whereEqualTo(STATUS_FIELD, filterOption.toString())
                        .orderBy(sortingField, order)
                        .startAfter(startAfterDoc)
                        .limit(increment)
                        .get()
                        .await()
                }
            } else {
                if(filterOption == TransactionsFragment.FilterOption.ALL) {
                    querySnapshot = database.collection(transactionsCollection)
                        .whereEqualTo(USER_FIELD, currentUserRef)
                        .orderBy(sortingField, order)
                        .limit(increment)
                        .get()
                        .await()
                } else {
                    querySnapshot = database.collection(transactionsCollection)
                        .whereEqualTo(USER_FIELD, currentUserRef)
                        .whereEqualTo(STATUS_FIELD, filterOption.toString())
                        .orderBy(sortingField, order)
                        .limit(increment)
                        .get()
                        .await()
                }
            }

            Log.d("FirestoreHandler", "Got ${querySnapshot.size()} transactions when getTransactions() was called")
            val transObjArr : ArrayList<TransactionModel> = arrayListOf()
            var lastDocument : DocumentSnapshot? = null

            if(!querySnapshot.isEmpty) {
                for (document in querySnapshot.documents) {
                    transObjArr.add(convertDataToTransactionObjSimplified(document)!!)
                }
                lastDocument = querySnapshot.documents.last()
            }

            return Pair(transObjArr, lastDocument)
        } catch(e: Exception) {
            Log.e("FirestoreHandler", "Error trying to get transactions from the database when getTransactions was called.")
            return null
        }
    }

    suspend fun getInitialTransactions(increment: Long) : Pair<ArrayList<TransactionModel>, DocumentSnapshot?>? {
        try {
            val authHandler = AuthHandler.getInstance(appContext)
            val currentUserId = authHandler.getUserUid()
            val currentUserRef = database.collection(usersCollection).document(currentUserId!!)

            val querySnapshot = database.collection(transactionsCollection)
                    .whereEqualTo(USER_FIELD, currentUserRef)
                    .orderBy(TRANSACTION_DATE_FIELD, Query.Direction.DESCENDING)
                    .limit(increment)
                    .get()
                    .await()

            Log.d("FirestoreHandler", "Got ${querySnapshot.size()} transactions when getTransactions() was called")
            val transObjArr : ArrayList<TransactionModel> = arrayListOf()
            var lastDocument : DocumentSnapshot? = null

            if(!querySnapshot.isEmpty) {
                for (document in querySnapshot.documents) {
                    transObjArr.add(convertDataToTransactionObjSimplified(document)!!)
                }
                lastDocument = querySnapshot.documents.last()
            }

            return Pair(transObjArr, lastDocument)
        } catch(e: Exception) {
            Log.e("FirestoreHandler", "Error trying to get transactions from the database when getTransactions was called.")
            return null
        }
    }

    suspend fun getTransactionsForAdmin(increment: Long,
                                filterOption: AdminTransactionsActivity.FilterOption,
                                startAfterDoc: DocumentSnapshot?) : Pair<ArrayList<TransactionModel>, DocumentSnapshot?>? {
        try {
            val querySnapshot : QuerySnapshot
            if(startAfterDoc != null) {
                if(filterOption == AdminTransactionsActivity.FilterOption.ALL) {
                    querySnapshot = database.collection(transactionsCollection)
                        .orderBy(TRANSACTION_DATE_FIELD, Query.Direction.DESCENDING)
                        .startAfter(startAfterDoc)
                        .limit(increment)
                        .get()
                        .await()
                } else {
                    querySnapshot = database.collection(transactionsCollection)
                        .whereEqualTo(STATUS_FIELD, filterOption.toString())
                        .orderBy(TRANSACTION_DATE_FIELD, Query.Direction.DESCENDING)
                        .startAfter(startAfterDoc)
                        .limit(increment)
                        .get()
                        .await()
                }
            } else {
                if(filterOption == AdminTransactionsActivity.FilterOption.ALL) {
                    querySnapshot = database.collection(transactionsCollection)
                        .orderBy(TRANSACTION_DATE_FIELD, Query.Direction.DESCENDING)
                        .limit(increment)
                        .get()
                        .await()
                } else {
                    querySnapshot = database.collection(transactionsCollection)
                        .whereEqualTo(STATUS_FIELD, filterOption.toString())
                        .orderBy(TRANSACTION_DATE_FIELD, Query.Direction.DESCENDING)
                        .limit(increment)
                        .get()
                        .await()
                }
            }

            Log.d("FirestoreHandler", "Got ${querySnapshot.size()} transactions when getTransactionsForAdmin() was called")
            val transObjArr : ArrayList<TransactionModel> = arrayListOf()
            var lastDocument : DocumentSnapshot? = null

            if(!querySnapshot.isEmpty) {
                for (document in querySnapshot.documents) {
                    transObjArr.add(convertDataToTransactionObj(document)!!)
                }
                lastDocument = querySnapshot.documents.last()
            }

            return Pair(transObjArr, lastDocument)
        } catch(e: Exception) {
            Log.e("FirestoreHandler", "Error trying to get transactions from the database when getTransactionsForAdmin() was called.")
            return null
        }
    }

    private fun truncateTimeFromDate(date: Date): Date {
        val calendar = Calendar.getInstance()
        calendar.time = date

        // Set time fields to zero
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        return calendar.time
    }

    private suspend fun convertDataToTransactionObjSimplified(document : DocumentSnapshot) : TransactionModel? {
        val data = document.data
        val bookRef = data?.get(BOOK_FIELD) as DocumentReference

        val book = bookRef.get().await().toObject(BookModel::class.java)

        // detect if book is already overdue
        var status = data[STATUS_FIELD].toString()
        val expectedReturnDate = data[EXPECTED_RETURN_DATE_FIELD] as Timestamp
        val expectedPickupDate = data[EXPECTED_PICKUP_DATE_FIELD] as Timestamp
        var cancelledDate = data[CANCELED_DATE_FIELD] as Timestamp?

        val expectedReturnDateWithoutTime = truncateTimeFromDate(expectedReturnDate.toDate())
        val dateTodayWithoutTime = truncateTimeFromDate(Timestamp.now().toDate())
        val expectedPickupDateWithoutTime = truncateTimeFromDate(expectedPickupDate.toDate())

        if(status == TransactionModel.Status.TO_RETURN.toString() && expectedReturnDateWithoutTime.before(dateTodayWithoutTime)) {
            updateTransaction(document.id, STATUS_FIELD, TransactionModel.Status.OVERDUE)
            status = TransactionModel.Status.OVERDUE.toString()
        } else if(status == TransactionModel.Status.FOR_PICKUP.toString() && expectedPickupDateWithoutTime.before(dateTodayWithoutTime)) {
            updateTransaction(document.id, STATUS_FIELD, TransactionModel.Status.CANCELLED)
            status = TransactionModel.Status.CANCELLED.toString()
            cancelledDate = Timestamp.now()
            updateTransaction(document.id, CANCELED_DATE_FIELD, cancelledDate)
        }

        if(book != null) {
            val transactionObject = TransactionModel(
                document.id,
                book,
                UserModel(), // simplify the model
                data[TRANSACTION_DATE_FIELD] as Timestamp,
                expectedPickupDate,
                expectedReturnDate,
                data[ACTUAL_PICKUP_DATE_FIELD] as Timestamp?,
                data[ACTUAL_RETURN_DATE_FIELD] as Timestamp?,
                cancelledDate,
                TransactionModel.Status.valueOf(status)
            )

            return transactionObject
        }
        return null
    }

    private suspend fun convertDataToTransactionObj(document : DocumentSnapshot) : TransactionModel? {
        val data = document.data
        val userRef = data?.get(USER_FIELD) as DocumentReference
        val bookRef = data[BOOK_FIELD] as DocumentReference

        val book = bookRef.get().await().toObject(BookModel::class.java)
        val userDoc = userRef.get().await()
        val userData = userDoc.data

        // detect if book is already overdue
        var status = data[STATUS_FIELD].toString()
        val expectedReturnDate = data[EXPECTED_RETURN_DATE_FIELD] as Timestamp
        val expectedPickupDate = data[EXPECTED_PICKUP_DATE_FIELD] as Timestamp
        var cancelledDate = data[CANCELED_DATE_FIELD] as Timestamp?

        val expectedReturnDateWithoutTime = truncateTimeFromDate(expectedReturnDate.toDate())
        val dateTodayWithoutTime = truncateTimeFromDate(Timestamp.now().toDate())
        val expectedPickupDateWithoutTime = truncateTimeFromDate(expectedPickupDate.toDate())

        if(status == TransactionModel.Status.TO_RETURN.toString() && expectedReturnDateWithoutTime.before(dateTodayWithoutTime)) {
            updateTransaction(document.id, STATUS_FIELD, TransactionModel.Status.OVERDUE)
            status = TransactionModel.Status.OVERDUE.toString()
        } else if(status == TransactionModel.Status.FOR_PICKUP.toString() && expectedPickupDateWithoutTime.before(dateTodayWithoutTime)) {
            updateTransaction(document.id, STATUS_FIELD, TransactionModel.Status.CANCELLED)
            status = TransactionModel.Status.CANCELLED.toString()
            cancelledDate = Timestamp.now()
            updateTransaction(document.id, CANCELED_DATE_FIELD, cancelledDate)
        }

        if(book != null) {
            val transactionObject = TransactionModel(
                document.id,
                book,
                UserModel(userData?.get(USER_ID_FIELD)!!.toString(), userData[FIRST_NAME_FIELD]!!.toString(), userData[LAST_NAME_FIELD]!!.toString()),
                data[TRANSACTION_DATE_FIELD] as Timestamp,
                expectedPickupDate,
                expectedReturnDate,
                data[ACTUAL_PICKUP_DATE_FIELD] as Timestamp?,
                data[ACTUAL_RETURN_DATE_FIELD] as Timestamp?,
                cancelledDate,
                TransactionModel.Status.valueOf(status)
            )

            return transactionObject
        }
        return null
    }

    suspend fun getTransactionsDetails() : HashMap<String, Long> {
        val authHandler = AuthHandler.getInstance(appContext)
        val currentUserId = authHandler.getUserUid()
        val currentUserRef = database.collection(usersCollection).document(currentUserId!!)

        val transactionsDetails = hashMapOf(
            "forPickup" to database.collection(transactionsCollection)
                .whereEqualTo(USER_FIELD, currentUserRef)
                .whereEqualTo(STATUS_FIELD, TransactionsFragment.FilterOption.FOR_PICKUP)
                .count().get(AggregateSource.SERVER).await().count,

            "toReturn" to database.collection(transactionsCollection)
                .whereEqualTo(USER_FIELD, currentUserRef)
                .whereEqualTo(STATUS_FIELD, TransactionsFragment.FilterOption.TO_RETURN)
                .count().get(AggregateSource.SERVER).await().count,

            "overdue" to database.collection(transactionsCollection)
                .whereEqualTo(USER_FIELD, currentUserRef)
                .whereEqualTo(STATUS_FIELD, TransactionsFragment.FilterOption.OVERDUE)
                .count().get(AggregateSource.SERVER).await().count,

            "returned" to database.collection(transactionsCollection)
                .whereEqualTo(USER_FIELD, currentUserRef)
                .whereEqualTo(STATUS_FIELD, TransactionsFragment.FilterOption.RETURNED)
                .count().get(AggregateSource.SERVER).await().count,
        )

        return transactionsDetails
    }

    suspend fun getAdminTransactionsDetails() : HashMap<String, Long> {
        val transactionsDetails = hashMapOf(
            "forPickup" to database.collection(transactionsCollection)
                .whereEqualTo(STATUS_FIELD, TransactionsFragment.FilterOption.FOR_PICKUP)
                .count().get(AggregateSource.SERVER).await().count,

            "toReturn" to database.collection(transactionsCollection)
                .whereEqualTo(STATUS_FIELD, TransactionsFragment.FilterOption.TO_RETURN)
                .count().get(AggregateSource.SERVER).await().count,

            "overdue" to database.collection(transactionsCollection)
                .whereEqualTo(STATUS_FIELD, TransactionsFragment.FilterOption.OVERDUE)
                .count().get(AggregateSource.SERVER).await().count,

            "returned" to database.collection(transactionsCollection)
                .whereEqualTo(STATUS_FIELD, TransactionsFragment.FilterOption.RETURNED)
                .count().get(AggregateSource.SERVER).await().count,
        )

        return transactionsDetails
    }

    /*** Books Collection ***/
    suspend fun createBook(bookId: String) {
        val bookRef = database.collection(booksCollection).document(bookId)
        val bookSnapshot = bookRef.get().await()
        if(bookSnapshot.exists()) {
            Log.d("FirestoreHandler", "Book $bookId already exists in Firestore")
        } else {
            val googleBooksAPIHandler = GoogleBooksAPIHandler()
            val bookObject = googleBooksAPIHandler.getBook(bookId)
            if(bookObject != null) {
                database.collection(booksCollection).document(bookId).set(bookObject)
                Log.d("FirestoreHandler", "Book $bookId successfully saved to the Firestore database")
            } else {
                Log.e("FirestoreHandler", "Error saving book $bookId to the Firestore database")
            }
        }
    }

    suspend fun getBook(bookId: String) : BookModel? {
        return try {
            val bookSnapshot = database.collection(booksCollection)
                .document(bookId)
                .get()
                .await()

            if (bookSnapshot.exists()) {
                bookSnapshot.toObject(BookModel::class.java)
            } else {
                Log.w("FirestoreHandler", "No book found in Firestore with id $bookId")
                null
            }
        } catch(e: Exception) {
            Log.e("FirestoreHandler", "Error getting book $bookId from Firestore when getBook() was called", e)
            null
        }
    }

    // used for storing back book ids into user's arrays in database using references
    // assumes book exists in database already
    private fun convertBookIdToDocRef(bookId: String): DocumentReference {
        return database.collection(booksCollection).document(bookId)
    }

    private fun convertBookIdsToDocRefs(bookIds: List<String>): ArrayList<DocumentReference> {
        val results = arrayListOf<DocumentReference>()
        bookIds.forEach { bookId ->
            results.add(convertBookIdToDocRef(bookId))
        }
        return results
    }

    // used for storing back book models into user's arrays in database using references
    // assumes book exists in database already
    private fun convertBookModelToDocRef(book: BookModel): DocumentReference {
        return database.collection(booksCollection).document(book.id)
    }

    private fun convertBookModelsToDocRefs(books: List<BookModel>): ArrayList<DocumentReference> {
        val results = arrayListOf<DocumentReference>()
        books.forEach { book ->
           results.add(convertBookModelToDocRef(book))
        }
        return results
    }
}