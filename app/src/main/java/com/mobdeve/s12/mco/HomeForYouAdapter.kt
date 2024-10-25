package com.mobdeve.s12.mco

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.mobdeve.s12.mco.databinding.ItemForYouCardBinding

class HomeForYouAdapter(private val data: ArrayList<BookModel>) : Adapter<HomeForYouViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeForYouViewHolder {
        val bookForYouCardBinding: ItemForYouCardBinding = ItemForYouCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false)
        val forYouCardViewHolder = HomeForYouViewHolder(bookForYouCardBinding)

        // TODO Later: Add on-click listener to the card that will launch an intent going to book details
        // TODO Later: Add on-click listener for Borrow -> should also launch an intent to book details but scrolled down
        // TODO MCO3: Add on-click listener for Favorite button that will add the book to the user's favorites list

        return forYouCardViewHolder
    }

    override fun onBindViewHolder(holder: HomeForYouViewHolder, position: Int) {
        holder.bindData(data[position])
    }

    override fun getItemCount(): Int {
        return data.size
    }

}