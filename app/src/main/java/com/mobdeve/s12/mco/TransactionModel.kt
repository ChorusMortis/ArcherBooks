package com.mobdeve.s12.mco

import java.util.Date

class TransactionModel(
    val transactionId: String,
    val book: BookModel,
    val user: UserModel,
    val transactionDate: Date,
    val expectedPickupDate: Date,
    val expectedReturnDate: Date,
    var actualPickupDate: Date,
    var actualReturnDate: Date,
    var canceledDate: Date?,
    var status: Status = Status.FOR_PICKUP
) {
    enum class Status {
        FOR_PICKUP,
        TO_RETURN,
        OVERDUE,
        CANCELLED,
        RETURNED
    }
}