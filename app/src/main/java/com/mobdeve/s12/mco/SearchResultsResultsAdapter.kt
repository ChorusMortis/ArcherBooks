package com.mobdeve.s12.mco

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mobdeve.s12.mco.databinding.ItemSearchresultsCardLightBinding

class SearchResultsResultsAdapter(private val data: ArrayList<BookModel>) : RecyclerView.Adapter<SearchResultsResultsViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SearchResultsResultsViewHolder {
        // TODO Later: Add on-click listener to the card that will launch an intent going to book details
        // TODO Later: Add on-click listener for Borrow -> should also launch an intent to book details but scrolled down
        // TODO MCO3: Add on-click listener for Favorite button that will add the book to the user's favorites list

        val viewBinding = ItemSearchresultsCardLightBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SearchResultsResultsViewHolder(viewBinding)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: SearchResultsResultsViewHolder, position: Int) {
        holder.bindData(data[position])
    }
}