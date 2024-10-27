package com.mobdeve.s12.mco

import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.mobdeve.s12.mco.databinding.ItemMyTransactionsCardLightBinding

class MyTransactionsTransViewHolder(private val viewBinding: ItemMyTransactionsCardLightBinding): RecyclerView.ViewHolder(viewBinding.root) {
    fun bindData(book: BookModel) {
        viewBinding.mytransactionscardIvCover.setImageResource(book.coverResource)
        viewBinding.mytransactionscardTvTitle.text = book.title

        var authors = ""
        book.authors.forEachIndexed { index, author ->
            authors += if(index == book.authors.size - 1) {
                author
            } else {
                "${author}, "
            }
        }
        viewBinding.mytransactionscardTvAuthors.text = "by $authors"

        if (book.hasTransaction == HasTransaction.NONE || book.hasTransaction == HasTransaction.INACTIVE) {
            viewBinding.mytransactionscardTvStatus.text = "Book Available" // TODO MCO3: Transfer this as an enum class to Transaction class
            viewBinding.mytransactionscardTvStatus.setTextColor(ContextCompat.getColor(viewBinding.root.context, R.color.book_available))
        } else {
            // TODO MCO3: Handle status when transaction is active (Pick up by..., Return by..., Overdue..., Unavailable...)
        }
    }
}