package com.mobdeve.s12.mco

import android.icu.util.Calendar
import com.mobdeve.s12.mco.TransactionModel.Status
import java.time.LocalDate
import java.util.Date

class TransactionGenerator {
    companion object {
        fun generateSampleTransactions(books: ArrayList<BookModel>, users: ArrayList<UserModel>) : ArrayList<TransactionModel> {
            val data = ArrayList<TransactionModel>()

            val calendar = Calendar.getInstance()
            calendar.set(2024, 11, 12)
            val date = calendar.time

            for(index in 0..9) {
                data.add(TransactionModel("transaction_1", books[index], users[index], date, date, date, date, date, null, Status.RETURNED ))
            }
            return data
        }
    }
}

















