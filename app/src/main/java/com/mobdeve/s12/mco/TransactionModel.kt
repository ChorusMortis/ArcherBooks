package com.mobdeve.s12.mco

import java.util.Date

class TransactionModel(
    val transactionId: String,
    val book: BookModel,
    val studentName: String,
    val expectedPickupDate: Date,
    val expectedReturnDate: Date,
    var status: Status = Status.PENDING
) {
    enum class Status {
        PENDING, // Waiting for pickup
        PICKED_UP, // Book has been picked up
        RETURNED, // Book has been returned
        CANCELLED, // Pickup cancelled
        PICKUP_MISSED, // Pickup deadline missed
        RETURN_MISSED // Return deadline missed
    }
}