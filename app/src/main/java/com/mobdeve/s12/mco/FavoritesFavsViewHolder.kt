package com.mobdeve.s12.mco

import androidx.recyclerview.widget.RecyclerView
import com.mobdeve.s12.mco.databinding.ItemFCardBinding

class FavoritesFavsViewHolder(private val viewBinding: ItemFCardBinding): RecyclerView.ViewHolder(viewBinding.root) {
    fun bindData(book: BookModel) {
        viewBinding.itemFvTvCover.setImageResource(book.coverResource)
        viewBinding.itemFvTvTitle.text = book.title
        viewBinding.itemFvTvAuthors.text = book.authors.joinToString(", ")
        viewBinding.itemFvTvPubyear.text = book.publishYear

        // if(book in current user's favorites list) -> select toggled on favorite button
        // else -> select toggled off
        viewBinding.itemFvIbFavbtn.setImageResource(R.drawable.icon_favorite_filled)
    }
}