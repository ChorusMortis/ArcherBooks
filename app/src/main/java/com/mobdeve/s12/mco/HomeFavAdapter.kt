package com.mobdeve.s12.mco

import android.content.Intent
import com.mobdeve.s12.mco.databinding.ItemFavCardLightBinding
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.mobdeve.s12.mco.databinding.ItemMbbLightBinding

class HomeFavAdapter(private val data: ArrayList<BookModel>) : Adapter<HomeFavViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeFavViewHolder {
        val itemFavCardBinding: ItemFavCardLightBinding = ItemFavCardLightBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false)
        val favCardViewHolder = HomeFavViewHolder(itemFavCardBinding)

        addListenerCard(favCardViewHolder, itemFavCardBinding)
        // TODO MCO3: Add on-click listener for Favorite button that will add the book to the user's favorites list

        return favCardViewHolder
    }

    override fun onBindViewHolder(holder: HomeFavViewHolder, position: Int) {
        holder.bindData(data[position])
    }

    override fun getItemCount(): Int {
        return data.size
    }

    private fun addListenerCard(holder : HomeFavViewHolder, itemFavCardBinding: ItemFavCardLightBinding) {
        holder.itemView.setOnClickListener(View.OnClickListener {
            val intent = Intent(holder.itemView.context, BookDetailsActivity::class.java)
            intent.putExtra(BookDetailsActivity.TITLE_KEY, data[holder.bindingAdapterPosition].title)
            intent.putExtra(BookDetailsActivity.YEAR_PUBLISHED_KEY, data[holder.bindingAdapterPosition].publishYear)
            intent.putExtra(BookDetailsActivity.AUTHORS_KEY, itemFavCardBinding.favcardTvAuthors.text) // concatenated with "by"
            intent.putExtra(BookDetailsActivity.COVER_KEY, data[holder.bindingAdapterPosition].coverResource)
            intent.putExtra(BookDetailsActivity.STATUS_KEY, "Book Available") // TODO: hardcoded for now, comes from transaction
            intent.putExtra(BookDetailsActivity.SHELF_LOCATION_KEY, data[holder.bindingAdapterPosition].shelfLocation)
            intent.putExtra(BookDetailsActivity.DESCRIPTION_KEY, data[holder.bindingAdapterPosition].description)
            intent.putExtra(BookDetailsActivity.PAGES_KEY, data[holder.bindingAdapterPosition].pageCount)
            holder.itemView.context.startActivity(intent)
        })
    }
}