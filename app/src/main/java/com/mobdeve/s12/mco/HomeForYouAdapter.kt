package com.mobdeve.s12.mco

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.mobdeve.s12.mco.databinding.ItemForYouCardLightBinding

class HomeForYouAdapter(private val data: ArrayList<BookModel>) : Adapter<HomeForYouViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeForYouViewHolder {
        val itemForYouCardBinding = ItemForYouCardLightBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false)
        val forYouCardViewHolder = HomeForYouViewHolder(itemForYouCardBinding)

        addListenerCard(forYouCardViewHolder, itemForYouCardBinding)
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

    private fun addListenerCard(holder : HomeForYouViewHolder, itemForYouCardBinding: ItemForYouCardLightBinding) {
        holder.itemView.setOnClickListener(View.OnClickListener {
            val intent = Intent(holder.itemView.context, BookInformationActivity::class.java)
            intent.putExtra(BookInformationActivity.TITLE_KEY, itemForYouCardBinding.foryoucardTvTitle.text)
            intent.putExtra(BookInformationActivity.YEAR_PUBLISHED_KEY, itemForYouCardBinding.foryoucardLightTvDate.text)
            intent.putExtra(BookInformationActivity.AUTHORS_KEY, itemForYouCardBinding.foryoucardTvAuthors.text)
            intent.putExtra(BookInformationActivity.COVER_KEY, data[holder.bindingAdapterPosition].coverResource)
            intent.putExtra(BookInformationActivity.STATUS_KEY, itemForYouCardBinding.foryoucardTvStatus.text)
            intent.putExtra(BookInformationActivity.SUBJECTS_KEY, data[holder.bindingAdapterPosition].subjects)
            intent.putExtra(BookInformationActivity.SHELF_LOCATION_KEY, data[holder.bindingAdapterPosition].shelfLocation)
            holder.itemView.context.startActivity(intent)
        })
    }
}