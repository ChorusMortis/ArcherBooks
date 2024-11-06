package com.mobdeve.s12.mco

import androidx.recyclerview.widget.RecyclerView
import com.mobdeve.s12.mco.databinding.ItemSrCardBinding

class SearchResultsResultsViewHolder(private val viewBinding: ItemSrCardBinding): RecyclerView.ViewHolder(viewBinding.root) {
    fun bindData(book: BookModel) {
        viewBinding.itemSrIvCover.setImageResource(book.coverResource)
        viewBinding.itemSrTvTitle.text = book.title
        viewBinding.itemSrAuthors.text = book.authors.joinToString(", ")

//        if (book.hasTransaction == HasTransaction.NONE || book.hasTransaction == HasTransaction.INACTIVE) {
//            viewBinding.searchresultscardTvStatus.text = "Book Available" // TODO MCO3: Transfer this as an enum class to Transaction class
//            viewBinding.searchresultscardTvStatus.setTextColor(ContextCompat.getColor(viewBinding.root.context, R.color.book_available))
//        } else {
//            // TODO MCO3: Handle status when transaction is active (Pick up by..., Return by..., Overdue..., Unavailable...)
//        }
//
//        // if(book in current user's favorites list) -> select toggled on favorite button
//        // else -> select toggled off
//        viewBinding.searchresultscardBtnFavorite.setImageResource(R.drawable.favorite_btn_toggled_off)
    }
}