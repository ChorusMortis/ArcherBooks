package com.mobdeve.s12.mco

import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.mobdeve.s12.mco.databinding.ItemFCardBinding

class FavoritesFavsViewHolder(private val viewBinding: ItemFCardBinding): RecyclerView.ViewHolder(viewBinding.root) {
    fun bindData(book: BookModel) {
        viewBinding.itemFIvCover.setImageResource(book.coverResource)
        viewBinding.itemFTvTitle.text = book.title
        viewBinding.itemFAuthors.text = book.authors.joinToString(", ")

        // if(book in current user's favorites list) -> select toggled on favorite button
        // else -> select toggled off
        viewBinding.itemFFavBtn.setImageResource(R.drawable.icon_favorite_filled)
    }
}