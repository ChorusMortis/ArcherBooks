package com.mobdeve.s12.mco

import android.content.res.ColorStateList
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.mobdeve.s12.mco.databinding.ItemSearchresultsResultsCardBinding

class SearchResultsResultsViewHolder(private val viewBinding: ItemSearchresultsResultsCardBinding): RecyclerView.ViewHolder(viewBinding.root) {
    fun bindData(book: BookModel) {
        viewBinding.itmSearchresultsIvBookcover.setImageResource(book.coverResource)
        viewBinding.itmSearchresultsTvBooktitle.text = book.title

        var authors = ""
        book.authors.forEachIndexed { index, author ->
            authors += if(index == book.authors.size - 1) {
                author
            } else {
                "${author}, "
            }
        }
        viewBinding.itmSearchresultsTvBookauthors.text = "by $authors"

        if(book.hasTransaction == HasTransaction.NONE || book.hasTransaction == HasTransaction.INACTIVE) {
            viewBinding.itmSearchresultsTvBookstatus.text = "Book Available" // TODO MCO3: Transfer this as an enum class to Transaction class
            viewBinding.itmSearchresultsTvBookstatus.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(viewBinding.root.context, R.color.book_available))
        } else {
            // TODO MCO3: Handle status when transaction is active (Pick up by..., Return by..., Overdue..., Unavailable...)
        }
    }
}