package com.mobdeve.s12.mco

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mobdeve.s12.mco.databinding.ItemMyFavoritesCardLightBinding

class FavoritesFavsAdapter(private val data: ArrayList<BookModel>): RecyclerView.Adapter<FavoritesFavsViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoritesFavsViewHolder {
        // TODO Later: Add on-click listener to the card that will launch an intent going to book details
        // TODO MCO3: Add on-click listener for Favorite button that will add the book to the user's favorites list

        val viewBinding = ItemMyFavoritesCardLightBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FavoritesFavsViewHolder(viewBinding)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: FavoritesFavsViewHolder, position: Int) {
        holder.bindData(data[position])
    }
}