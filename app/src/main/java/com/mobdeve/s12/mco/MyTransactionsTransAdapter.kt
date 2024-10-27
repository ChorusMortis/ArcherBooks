package com.mobdeve.s12.mco

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mobdeve.s12.mco.databinding.ItemMyTransactionsCardLightBinding

class MyTransactionsTransAdapter(private val data: ArrayList<BookModel>): RecyclerView.Adapter<MyTransactionsTransViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyTransactionsTransViewHolder {
        // TODO Later: Add on-click listener to the card that will launch an intent going to book details
        // TODO Later: Add on-click listener for Borrow -> should also launch an intent to book details but scrolled down

        val viewBinding = ItemMyTransactionsCardLightBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyTransactionsTransViewHolder(viewBinding)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: MyTransactionsTransViewHolder, position: Int) {
        holder.bindData(data[position])
    }
}