package com.mobdeve.s12.mco

import com.mobdeve.s12.mco.databinding.ItemFavCardLightBinding
import androidx.recyclerview.widget.RecyclerView

class HomeFavViewHolder(private val viewBinding: ItemFavCardLightBinding): RecyclerView.ViewHolder(viewBinding.root) {

    fun bindData(book: BookModel) {
        this.viewBinding.favcardIvCover.setImageResource(book.coverResource)
        this.viewBinding.favcardTvTitle.text = book.title

        var authors = ""
        book.authors.forEachIndexed { index, author ->
            authors += if(index == book.authors.size - 1) {
                author
            } else {
                "${author}, "
            }
        }
        this.viewBinding.favcardTvAuthors.text = "by $authors"

//        if(book in current user's favorites list) -> select toggled on favorite button
//        else -> select toggled off
        this.viewBinding.favcardBtnFavorite.setImageResource(R.drawable.favorite_btn_toggled_on)
    }
}