package com.mobdeve.s12.mco

import com.google.firebase.Timestamp

class TransactionModel {

    var transactionId: String = "-1"
    lateinit var book: BookModel
        private set
    lateinit var user: UserModel
        private set
    lateinit var transactionDate: Timestamp
        private set
    lateinit var expectedPickupDate: Timestamp
        private set
    lateinit var expectedReturnDate: Timestamp
        private set
    var actualPickupDate: Timestamp? = null
    var actualReturnDate: Timestamp? = null
    var canceledDate: Timestamp? = null
    var status: Status = Status.FOR_PICKUP

    enum class Status {
        FOR_PICKUP,
        TO_RETURN,
        OVERDUE,
        CANCELLED,
        RETURNED
    }

    constructor()
    
    constructor(transactionId: String,
                   book: BookModel,
                   user: UserModel,
                   transactionDate: Timestamp,
                   expectedPickupDate: Timestamp,
                   expectedReturnDate: Timestamp,
                   actualPickupDate: Timestamp?,
                   actualReturnDate: Timestamp?,
                   canceledDate: Timestamp?,
                   status: Status) {
        this.transactionId = transactionId
        this.book = book
        this.user = user
        this.transactionDate = transactionDate
        this.expectedPickupDate = expectedPickupDate
        this.expectedReturnDate = expectedReturnDate
        this.actualPickupDate = actualPickupDate
        this.actualReturnDate = actualReturnDate
        this.canceledDate = canceledDate
        this.status = status
    }

    constructor(book: BookModel,
                user: UserModel,
                transactionDate: Timestamp,
                expectedPickupDate: Timestamp,
                expectedReturnDate: Timestamp,
                actualPickupDate: Timestamp?,
                actualReturnDate: Timestamp?,
                canceledDate: Timestamp?,
                status: Status) {
        this.book = book
        this.user = user
        this.transactionDate = transactionDate
        this.expectedPickupDate = expectedPickupDate
        this.expectedReturnDate = expectedReturnDate
        this.actualPickupDate = actualPickupDate
        this.actualReturnDate = actualReturnDate
        this.canceledDate = canceledDate
        this.status = status
    }
}