package com.mobdeve.s12.mco

import android.icu.util.Calendar
import com.google.firebase.Timestamp
import com.mobdeve.s12.mco.TransactionModel.Status

class TransactionGenerator {
    companion object {
        fun generateSampleTransactions(books: ArrayList<BookModel>, users: ArrayList<UserModel>) : ArrayList<TransactionModel> {
            val data = ArrayList<TransactionModel>()

            val calendar = Calendar.getInstance()
            calendar.set(2024, 11, 12)
            val date = calendar.time
            val timestamp = Timestamp(date)

            for(index in 0..9) {
                if(index in intArrayOf(3, 6, 9)) {
                    data.add(TransactionModel("transaction_1", books[index], users[index], timestamp, timestamp, timestamp, timestamp, timestamp, null, Status.RETURNED ))
                } else if(index in intArrayOf(2, 5, 8)) {
                    data.add(TransactionModel("transaction_1", books[index], users[index], timestamp, timestamp, timestamp, timestamp, null, null, Status.TO_RETURN ))
                } else {
                    data.add(TransactionModel("transaction_1", books[index], users[index], timestamp, timestamp, timestamp, null, null, null, Status.FOR_PICKUP))
                }

            }
            return data
        }
    }
}

















