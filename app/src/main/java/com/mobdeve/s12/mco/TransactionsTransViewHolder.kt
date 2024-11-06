package com.mobdeve.s12.mco

import android.icu.util.Calendar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.mobdeve.s12.mco.databinding.ItemTsCardBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TransactionsTransViewHolder(private val viewBinding: ItemTsCardBinding): RecyclerView.ViewHolder(viewBinding.root) {
    fun bindData(book: BookModel) {
        viewBinding.itemTsTvCover.setImageResource(book.coverResource)
        viewBinding.itemTsTvTitle.text = book.title
        viewBinding.itemTsTvAuthors.text = book.authors.joinToString(", ")

        // TODO MCO3: Pass Transaction array list to adapter, then pass the specific transaction to ViewHolder via bindData fun
        // Then, get transactionDate, status (To pick up, to return, overdue, cancelled, returned)
        // Based on status, replace the label
        // Based on status, query the matching date (pickupDate, expectedReturnDate, expectedReturnDate, cancelledDate, actualReturnDate)
        // Based on status, replace the icon
        viewBinding.itemTsTvTransDateValue.text = formatDate(Date())
        viewBinding.itemTsIvBorrowStatusIcon.setImageResource(R.drawable.icon_timer)
        viewBinding.itemTsTvBorrowStatusLabel.text = viewBinding.root.context.getString(R.string.to_return)

        val calendar = Calendar.getInstance()
        calendar.set(2024, 11, 24)
        viewBinding.itemTsTvBorrowStatusDate.text = formatDate(calendar.time)
    }

    private fun formatDate(date: Date): String {
        val dateFormat = "MMM d, yyyy"
        val simpleDateFormat = SimpleDateFormat(dateFormat, Locale.US)
        return simpleDateFormat.format(date)
    }
}