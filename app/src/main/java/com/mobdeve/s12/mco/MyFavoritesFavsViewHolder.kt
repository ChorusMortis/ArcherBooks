package com.mobdeve.s12.mco

import androidx.recyclerview.widget.RecyclerView
import com.mobdeve.s12.mco.databinding.ItemMyFavoritesCardLightBinding

class MyFavoritesFavsViewHolder(private val viewBinding: ItemMyFavoritesCardLightBinding): RecyclerView.ViewHolder(viewBinding.root) {
    fun bindData(book: BookModel) {
        viewBinding.myfavoritescardIvCover.setImageResource(book.coverResource)
        viewBinding.myfavoritescardTvTitle.text = book.title

        var authors = ""
        book.authors.forEachIndexed { index, author ->
            authors += if(index == book.authors.size - 1) {
                author
            } else {
                "${author}, "
            }
        }
        viewBinding.myfavoritescardTvAuthors.text = "by $authors"

        // if(book in current user's favorites list) -> select toggled on favorite button
        // else -> select toggled off
        viewBinding.myfavoritescardBtnFavorite.setImageResource(R.drawable.favorite_btn_toggled_off)
    }
}