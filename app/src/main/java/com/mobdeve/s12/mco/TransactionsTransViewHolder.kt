package com.mobdeve.s12.mco

import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.mobdeve.s12.mco.databinding.ItemTransactionsCardLightBinding

class TransactionsTransViewHolder(private val viewBinding: ItemTransactionsCardLightBinding): RecyclerView.ViewHolder(viewBinding.root) {
    fun bindData(book: BookModel) {
        viewBinding.mytransactionscardIvCover.setImageResource(book.coverResource)
        viewBinding.mytransactionscardTvTitle.text = book.title
        viewBinding.mytransactionscardTvAuthors.text = book.authors.joinToString(", ")

        if (book.hasTransaction == HasTransaction.NONE || book.hasTransaction == HasTransaction.INACTIVE) {
            viewBinding.mytransactionscardTvStatus.text = "Book Available" // TODO MCO3: Transfer this as an enum class to Transaction class
            viewBinding.mytransactionscardTvStatus.setTextColor(ContextCompat.getColor(viewBinding.root.context, R.color.book_available))
        } else {
            // TODO MCO3: Handle status when transaction is active (Pick up by..., Return by..., Overdue..., Unavailable...)
        }
    }
}