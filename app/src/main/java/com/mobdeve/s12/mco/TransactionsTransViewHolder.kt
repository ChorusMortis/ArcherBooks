package com.mobdeve.s12.mco

import android.icu.util.Calendar
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.Timestamp
import com.mobdeve.s12.mco.BookDetailsActivity.Companion.COVER_KEY
import com.mobdeve.s12.mco.databinding.ItemTsCardBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TransactionsTransViewHolder(private val viewBinding: ItemTsCardBinding): RecyclerView.ViewHolder(viewBinding.root) {
    fun bindData(transaction: TransactionModel) {
        Glide.with(viewBinding.root.context)
            .load(transaction.book.coverResource)
            .into(viewBinding.itemTsTvCover)
        viewBinding.itemTsTvTitle.text = transaction.book.title
        viewBinding.itemTsTvAuthors.text = transaction.book.authors.joinToString(", ")

        setStatusColor(transaction)
        setStatusIcon(transaction)
        setStatusLabel(transaction)
        setStatusDateValue(transaction)
    }

    private fun setStatusColor(transaction: TransactionModel) {
        if(transaction.status == TransactionModel.Status.CANCELLED) {
            viewBinding.itemTsTvBorrowStatusLabel.setTextColor(ContextCompat.getColor(viewBinding.root.context, R.color.book_unavailable))
            viewBinding.itemTsTvBorrowStatusDate.setTextColor(ContextCompat.getColor(viewBinding.root.context, R.color.book_unavailable))
        } else if(transaction.status == TransactionModel.Status.RETURNED) {
            viewBinding.itemTsTvBorrowStatusLabel.setTextColor(ContextCompat.getColor(viewBinding.root.context, R.color.book_available))
            viewBinding.itemTsTvBorrowStatusDate.setTextColor(ContextCompat.getColor(viewBinding.root.context, R.color.book_available))
        } else {
            viewBinding.itemTsTvBorrowStatusLabel.setTextColor(ContextCompat.getColor(viewBinding.root.context, R.color.book_borrowed))
            viewBinding.itemTsTvBorrowStatusDate.setTextColor(ContextCompat.getColor(viewBinding.root.context, R.color.book_borrowed))
        }
    }

    private fun setStatusIcon(transaction: TransactionModel) {
        if(transaction.status == TransactionModel.Status.CANCELLED) {
            viewBinding.itemTsIvBorrowStatusIcon.setImageResource(R.drawable.icon_cancel_transaction)
        } else if(transaction.status == TransactionModel.Status.RETURNED) {
            viewBinding.itemTsIvBorrowStatusIcon.setImageResource(R.drawable.icon_returned)
        } else {
            viewBinding.itemTsIvBorrowStatusIcon.setImageResource(R.drawable.icon_timer)
        }
    }

    private fun setStatusDateValue(transaction: TransactionModel) {
        when(transaction.status) {
            TransactionModel.Status.FOR_PICKUP -> viewBinding.itemTsTvBorrowStatusLabel.text = viewBinding.root.context.getString(R.string.for_pickup)
            TransactionModel.Status.TO_RETURN -> viewBinding.itemTsTvBorrowStatusLabel.text = viewBinding.root.context.getString(R.string.to_return)
            TransactionModel.Status.RETURNED -> viewBinding.itemTsTvBorrowStatusLabel.text = viewBinding.root.context.getString(R.string.returned)
            TransactionModel.Status.OVERDUE -> viewBinding.itemTsTvBorrowStatusLabel.text = viewBinding.root.context.getString(R.string.overdue)
            TransactionModel.Status.CANCELLED -> viewBinding.itemTsTvBorrowStatusLabel.text = viewBinding.root.context.getString(R.string.cancelled)
        }
    }

    private fun setStatusLabel(transaction: TransactionModel) {
        viewBinding.itemTsTvTransDateValue.text = convertTimestampToString(transaction.transactionDate)

        when(transaction.status) {
            TransactionModel.Status.FOR_PICKUP -> viewBinding.itemTsTvBorrowStatusDate.text = convertTimestampToString(transaction.expectedPickupDate)
            TransactionModel.Status.TO_RETURN -> viewBinding.itemTsTvBorrowStatusLabel.text = convertTimestampToString(transaction.expectedReturnDate)
            TransactionModel.Status.RETURNED -> viewBinding.itemTsTvBorrowStatusLabel.text = convertTimestampToString(transaction.actualReturnDate!!)
            TransactionModel.Status.OVERDUE -> viewBinding.itemTsTvBorrowStatusLabel.text = convertTimestampToString(transaction.expectedReturnDate)
            TransactionModel.Status.CANCELLED -> viewBinding.itemTsTvBorrowStatusLabel.text = convertTimestampToString(transaction.canceledDate!!)
        }
    }

    private fun convertTimestampToString(timestamp: Timestamp) : String {
        val dateFormat = "MMM d, yyyy"
        val formatter = SimpleDateFormat(dateFormat, Locale.ENGLISH)
        return formatter.format(timestamp.toDate())

    }
}