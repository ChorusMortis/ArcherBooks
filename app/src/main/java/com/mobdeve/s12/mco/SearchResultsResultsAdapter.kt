package com.mobdeve.s12.mco

import android.content.Intent
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mobdeve.s12.mco.databinding.ItemSrCardBinding

class SearchResultsResultsAdapter(private val data: ArrayList<BookModel>) : RecyclerView.Adapter<SearchResultsResultsViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchResultsResultsViewHolder {
        val itemSearchResultsCardBinding = ItemSrCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val srViewHolder = SearchResultsResultsViewHolder(itemSearchResultsCardBinding)
        addListenerCard(srViewHolder, itemSearchResultsCardBinding)

        if(srViewHolder.bindingAdapterPosition % 2 == 0) {
            srViewHolder.itemView.foregroundGravity = Gravity.START
        } else {
            srViewHolder.itemView.foregroundGravity = Gravity.END
        }
        return srViewHolder
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: SearchResultsResultsViewHolder, position: Int) {
        holder.bindData(data[position])
    }

    private fun addListenerCard(holder : SearchResultsResultsViewHolder, itemSearchResultsCardBinding: ItemSrCardBinding) {
        holder.itemView.setOnClickListener(View.OnClickListener {
            val intent = Intent(holder.itemView.context, BookDetailsActivity::class.java)
            intent.putExtra(BookDetailsActivity.TITLE_KEY, data[holder.bindingAdapterPosition].title)
            intent.putExtra(BookDetailsActivity.YEAR_PUBLISHED_KEY, data[holder.bindingAdapterPosition].publishYear)
            intent.putExtra(BookDetailsActivity.AUTHORS_KEY, data[holder.bindingAdapterPosition].authors.joinToString(", "))
            intent.putExtra(BookDetailsActivity.COVER_KEY, data[holder.bindingAdapterPosition].coverResource)
            intent.putExtra(BookDetailsActivity.PUBLISHER_KEY, data[holder.bindingAdapterPosition].publisher)
            intent.putExtra(BookDetailsActivity.STATUS_KEY, "Book Available") // TODO MCO3 comes from transaction
            intent.putExtra(BookDetailsActivity.SHELF_LOCATION_KEY, data[holder.bindingAdapterPosition].shelfLocation)
            intent.putExtra(BookDetailsActivity.DESCRIPTION_KEY, data[holder.bindingAdapterPosition].description)
            intent.putExtra(BookDetailsActivity.PAGES_KEY, data[holder.bindingAdapterPosition].pageCount)
            holder.itemView.context.startActivity(intent)
        })
    }

    fun addBooks(newBooks: ArrayList<BookModel>) {
        val startingIndex = data.size
        data.addAll(newBooks)
        notifyItemRangeInserted(startingIndex, newBooks.size)
    }
}