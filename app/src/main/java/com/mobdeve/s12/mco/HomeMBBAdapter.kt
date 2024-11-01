package com.mobdeve.s12.mco

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.mobdeve.s12.mco.databinding.ItemForYouCardLightBinding
import com.mobdeve.s12.mco.databinding.ItemMbbLightBinding

class HomeMBBAdapter(private val data: ArrayList<BookModel>) : Adapter<HomeMBBViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeMBBViewHolder {
        val itemMBBCardBinding: ItemMbbLightBinding = ItemMbbLightBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false)
        val mbbViewHolder = HomeMBBViewHolder(itemMBBCardBinding)

        addListenerCard(mbbViewHolder, itemMBBCardBinding)

        return mbbViewHolder
    }

    override fun onBindViewHolder(holder: HomeMBBViewHolder, position: Int) {
        holder.bindData(data[position])
    }

    override fun getItemCount(): Int {
        return data.size
    }

    private fun addListenerCard(holder : HomeMBBViewHolder, itemMBBCardBinding: ItemMbbLightBinding) {
        holder.itemView.setOnClickListener(View.OnClickListener {
            val intent = Intent(holder.itemView.context, BookDetailsActivity::class.java)
            intent.putExtra(BookDetailsActivity.TITLE_KEY, data[holder.bindingAdapterPosition].title)
            intent.putExtra(BookDetailsActivity.YEAR_PUBLISHED_KEY, data[holder.bindingAdapterPosition].publishYear)
            intent.putExtra(BookDetailsActivity.AUTHORS_KEY, itemMBBCardBinding.mbbTvAuthors.text) // concatenated with "by"
            intent.putExtra(BookDetailsActivity.COVER_KEY, data[holder.bindingAdapterPosition].coverResource)
            intent.putExtra(BookDetailsActivity.STATUS_KEY, itemMBBCardBinding.mbbTvStatus.text) // comes from transaction
            intent.putExtra(BookDetailsActivity.SHELF_LOCATION_KEY, data[holder.bindingAdapterPosition].shelfLocation)
            intent.putExtra(BookDetailsActivity.DESCRIPTION_KEY, data[holder.bindingAdapterPosition].description)
            intent.putExtra(BookDetailsActivity.PAGES_KEY, data[holder.bindingAdapterPosition].pageCount)
            holder.itemView.context.startActivity(intent)
        })
    }
}