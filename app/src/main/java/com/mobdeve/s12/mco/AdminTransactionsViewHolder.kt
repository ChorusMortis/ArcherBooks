package com.mobdeve.s12.mco

import android.content.res.ColorStateList
import com.mobdeve.s12.mco.databinding.ItemAdminTsCardBinding
import android.icu.util.Calendar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AdminTransactionsViewHolder(private val viewBinding: ItemAdminTsCardBinding): RecyclerView.ViewHolder(viewBinding.root) {
    fun bindData(transaction: TransactionModel) {
        // TODO MCO3: Get from transactions collection

        viewBinding.itemAdminTsTvCover.setImageResource(transaction.book.coverResource)
        viewBinding.itemAdminTsTvTitle.text = transaction.book.title
        viewBinding.itemAdminTsTvBorrowerName.text = transaction.user.firstName + " " + transaction.user.lastName

        val calendar = Calendar.getInstance()
        calendar.set(2024, 11, 24)
        viewBinding.itemAdminTsTvTransDateValue.text = formatDate(calendar.time)
        modifyTransactionDetails(transaction)
        setEditStatusButtonIcon(transaction)
    }

    private fun formatDate(date: Date): String {
        val dateFormat = "MMM d, yyyy"
        val simpleDateFormat = SimpleDateFormat(dateFormat, Locale.US)
        return simpleDateFormat.format(date)
    }

    private fun modifyTransactionDetails(transaction: TransactionModel) {
        when (transaction.status) {
            TransactionModel.Status.FOR_PICKUP -> {
                setStatusLabelAndVal(R.drawable.icon_timer, R.string.for_pickup, R.color.book_borrowed, transaction.expectedPickupDate)
            }
            TransactionModel.Status.TO_RETURN -> {
                setStatusLabelAndVal(R.drawable.icon_timer, R.string.to_return, R.color.book_borrowed, transaction.expectedReturnDate)
            }
            TransactionModel.Status.RETURNED -> {
                setStatusLabelAndVal(R.drawable.icon_returned, R.string.returned, R.color.book_available, transaction.actualReturnDate!!)
            }
            TransactionModel.Status.CANCELLED -> {
                setStatusLabelAndVal(R.drawable.icon_cancel_transaction, R.string.cancelled, R.color.book_unavailable, transaction.canceledDate!!)
            }
            TransactionModel.Status.OVERDUE -> {
                setStatusLabelAndVal(R.drawable.icon_overdue, R.string.overdue, R.color.book_borrowed, transaction.expectedReturnDate)
            }
        }
    }

    private fun setStatusLabelAndVal(icon: Int, statusLabel: Int, statusColor: Int, statusDate: Date) {
        viewBinding.itemAdminTsIvBorrowStatusIcon.setImageResource(icon)
        viewBinding.itemAdminTsTvBorrowStatusLabel.text = viewBinding.root.context.getString(statusLabel)
        viewBinding.itemAdminTsTvBorrowStatusLabel.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(viewBinding.root.context, statusColor)))
        viewBinding.itemAdminTsTvBorrowStatusDate.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(viewBinding.root.context, statusColor)))
        viewBinding.itemAdminTsTvBorrowStatusDate.text = formatDate(statusDate)
    }

    private fun setEditStatusButtonIcon(transaction: TransactionModel) {
        when (transaction.status) {
            TransactionModel.Status.FOR_PICKUP -> {
                viewBinding.itemAdminEditBtn.setImageResource(R.drawable.icon_for_pickup_edit)
                viewBinding.itemAdminEditBtn.isEnabled = true
                viewBinding.itemAdminEditBtn.alpha = 1F
            }
            TransactionModel.Status.TO_RETURN -> {
                viewBinding.itemAdminEditBtn.setImageResource(R.drawable.icon_to_return_edit)
                viewBinding.itemAdminEditBtn.isEnabled = true
                viewBinding.itemAdminEditBtn.alpha = 1F
            }
            TransactionModel.Status.RETURNED -> {
                viewBinding.itemAdminEditBtn.setImageResource(R.drawable.icon_available)
                viewBinding.itemAdminEditBtn.isEnabled = false
                viewBinding.itemAdminEditBtn.alpha = 0.3F
            }
            TransactionModel.Status.CANCELLED -> {
                viewBinding.itemAdminEditBtn.setImageResource(R.drawable.icon_cancel_transaction)
                viewBinding.itemAdminEditBtn.isEnabled = false
                viewBinding.itemAdminEditBtn.alpha = 1F
            }
            TransactionModel.Status.OVERDUE -> {
                viewBinding.itemAdminEditBtn.setImageResource(R.drawable.icon_to_return_edit)
                viewBinding.itemAdminEditBtn.isEnabled = true
                viewBinding.itemAdminEditBtn.alpha = 1F
            }
        }
    }
}