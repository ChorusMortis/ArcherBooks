package com.mobdeve.s12.mco

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mobdeve.s12.mco.databinding.ItemMyTransactionsCardLightBinding

class TransactionsTransAdapter(private val data: ArrayList<BookModel>): RecyclerView.Adapter<TransactionsTransViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TransactionsTransViewHolder {
        // TODO Later: Add on-click listener to the card that will launch an intent going to book details
        // TODO Later: Add on-click listener for Borrow -> should also launch an intent to book details but scrolled down

        val viewBinding = ItemMyTransactionsCardLightBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TransactionsTransViewHolder(viewBinding)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: TransactionsTransViewHolder, position: Int) {
        holder.bindData(data[position])
    }
}