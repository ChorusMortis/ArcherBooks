package com.mobdeve.s12.mco

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mobdeve.s12.mco.databinding.ItemRvCardBinding

class HomeRVViewHolder(private val viewBinding: ItemRvCardBinding): RecyclerView.ViewHolder(viewBinding.root) {

    fun bindData(book: BookModel) {
//        this.viewBinding.itemViewedRecentlyIvCover.setImageResource(book.coverResource)
        Glide.with(viewBinding.root.context)
            .load(book.coverResource)
            .into(viewBinding.itemViewedRecentlyIvCover)
        this.viewBinding.itemViewedRecentlyTvTitle.text = book.title
        this.viewBinding.itemViewedRecentlyAuthors.text = book.authors.joinToString(", ")
    }
}