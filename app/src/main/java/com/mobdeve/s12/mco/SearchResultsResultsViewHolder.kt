package com.mobdeve.s12.mco

import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mobdeve.s12.mco.BookDetailsActivity.Companion.ID_KEY
import com.mobdeve.s12.mco.databinding.ItemSrCardBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SearchResultsResultsViewHolder(private val viewBinding: ItemSrCardBinding): RecyclerView.ViewHolder(viewBinding.root) {
    fun bindData(book: BookModel) {
        Glide.with(viewBinding.root.context)
            .load(book.coverResource)
            .into(viewBinding.itemSrIvCover)
        viewBinding.itemSrTvTitle.text = book.title
        viewBinding.itemSrAuthors.text = book.authors.joinToString(", ")
        viewBinding.itemSrYear.text = book.publishYear

        setStatusIcon(book)
    }

    private fun setStatusIcon(book: BookModel) {
        CoroutineScope(Dispatchers.Main).launch {
            // show progress bar
            viewBinding.itemSearchResultsStatusIcon.setColorFilter(ContextCompat.getColor(viewBinding.root.context, R.color.white))
            viewBinding.itemSrProgressBar.visibility = View.VISIBLE

            // get backend handlers
            val firestoreHandler = FirestoreHandler.getInstance(viewBinding.root.context)
            val authHandler = AuthHandler.getInstance(viewBinding.root.context)

            // get transaction and user data
            val latestTransactionOfBook = firestoreHandler?.getLatestTransaction(book.id)
            val currentUserId = authHandler?.getCurrentUser()?.uid

            // default styling (for unavailable book status)
            var statusResource = R.drawable.icon_unavailable

            // modify styling if status is available or borrowed
            if(latestTransactionOfBook == null ||
                latestTransactionOfBook.status == TransactionModel.Status.CANCELLED ||
                latestTransactionOfBook.status == TransactionModel.Status.RETURNED)  {
                statusResource = R.drawable.icon_available
            } else if(latestTransactionOfBook.user.userId == currentUserId) {
                statusResource = R.drawable.icon_timer
            }

            // set appropriate styling
            viewBinding.itemSearchResultsStatusIcon.setImageResource(statusResource)

            // hide progress bar
            viewBinding.itemSearchResultsStatusIcon.clearColorFilter()
            viewBinding.itemSrProgressBar.visibility = View.GONE
        }
    }
}