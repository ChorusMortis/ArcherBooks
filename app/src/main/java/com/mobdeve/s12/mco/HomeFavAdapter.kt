package com.mobdeve.s12.mco

import com.mobdeve.s12.mco.databinding.ItemFavCardLightBinding
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter

class HomeFavAdapter(private val data: ArrayList<BookModel>) : Adapter<HomeFavViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeFavViewHolder {
        val itemFavCardBinding: ItemFavCardLightBinding = ItemFavCardLightBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false)
        val favCardViewHolder = HomeFavViewHolder(itemFavCardBinding)

        // TODO Later: Add on-click listener to the card that will launch an intent going to book details
        // TODO MCO3: Add on-click listener for Favorite button that will add the book to the user's favorites list

        return favCardViewHolder
    }

    override fun onBindViewHolder(holder: HomeFavViewHolder, position: Int) {
        holder.bindData(data[position])
    }

    override fun getItemCount(): Int {
        return data.size
    }
}