package com.mobdeve.s12.mco

import androidx.recyclerview.widget.RecyclerView
import com.mobdeve.s12.mco.databinding.ItemRvCardBinding

class HomeRVViewHolder(private val viewBinding: ItemRvCardBinding): RecyclerView.ViewHolder(viewBinding.root) {

    fun bindData(book: BookModel) {
        this.viewBinding.itemViewedRecentlyIvCover.setImageResource(book.coverResource)
        this.viewBinding.itemViewedRecentlyTvTitle.text = book.title

        var authors = ""
        book.authors.forEachIndexed { index, author ->
            authors += if(index == book.authors.size - 1) {
                author
            } else {
                "${author}, "
            }
        }
        this.viewBinding.itemViewedRecentlyAuthors.text = authors
    }
}