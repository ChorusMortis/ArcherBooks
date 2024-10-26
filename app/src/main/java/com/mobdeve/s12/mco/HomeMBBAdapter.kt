package com.mobdeve.s12.mco

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.mobdeve.s12.mco.databinding.ItemMbbLightBinding

class HomeMBBAdapter(private val data: ArrayList<BookModel>) : Adapter<HomeMBBViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeMBBViewHolder {
        val itemMBBCardBinding: ItemMbbLightBinding = ItemMbbLightBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false)
        val mbbViewHolder = HomeMBBViewHolder(itemMBBCardBinding)

        // TODO Later: Add on-click listener to the card that will launch an intent going to book details

        return mbbViewHolder
    }

    override fun onBindViewHolder(holder: HomeMBBViewHolder, position: Int) {
        holder.bindData(data[position])
        holder.setFonts()
    }

    override fun getItemCount(): Int {
        return data.size
    }
}