package com.mobdeve.s12.mco

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mobdeve.s12.mco.databinding.ItemSrCardBinding

class SearchResultsResultsViewHolder(private val viewBinding: ItemSrCardBinding): RecyclerView.ViewHolder(viewBinding.root) {
    fun bindData(book: BookModel) {
//        viewBinding.itemSrIvCover.setImageResource(book.coverResource)
        Glide.with(viewBinding.root.context)
            .load(book.coverResource)
            .into(viewBinding.itemSrIvCover)
        viewBinding.itemSrTvTitle.text = book.title
        viewBinding.itemSrAuthors.text = book.authors.joinToString(", ")
        viewBinding.itemSrYear.text = book.publishYear

        // TODO MCO3: Pass Transaction array list to adapter, then pass the specific transaction to ViewHolder via bindData fun
        // Then, create a conditional to properly check icon to set
        viewBinding.itemSearchResultsStatusIcon.setImageResource(R.drawable.icon_available)
    }
}