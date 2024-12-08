package com.mobdeve.s12.mco

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mobdeve.s12.mco.databinding.ItemTsCardBinding

class TransactionsTransAdapter(private val data: ArrayList<TransactionModel>): RecyclerView.Adapter<TransactionsTransViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionsTransViewHolder {
        val itemTransViewBinding = ItemTsCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val transViewHolder = TransactionsTransViewHolder(itemTransViewBinding)
        addListenerCard(transViewHolder, itemTransViewBinding)

        return transViewHolder
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: TransactionsTransViewHolder, position: Int) {
        holder.bindData(data[position])
    }

    private fun addListenerCard(holder : TransactionsTransViewHolder, itemTransViewBinding: ItemTsCardBinding) {
        holder.itemView.setOnClickListener(View.OnClickListener {
            val intent = Intent(holder.itemView.context, BookDetailsActivity::class.java)
            intent.putExtra(BookDetailsActivity.ID_KEY, data[holder.bindingAdapterPosition].book.id)
            intent.putExtra(BookDetailsActivity.TITLE_KEY, data[holder.bindingAdapterPosition].book.title)
            intent.putExtra(BookDetailsActivity.YEAR_PUBLISHED_KEY, data[holder.bindingAdapterPosition].book.publishYear)
            intent.putExtra(BookDetailsActivity.AUTHORS_KEY, data[holder.bindingAdapterPosition].book.authors.joinToString(", "))
            intent.putExtra(BookDetailsActivity.COVER_KEY, data[holder.bindingAdapterPosition].book.coverResource)
            intent.putExtra(BookDetailsActivity.PUBLISHER_KEY, data[holder.bindingAdapterPosition].book.publisher)
            intent.putExtra(BookDetailsActivity.SHELF_LOCATION_KEY, data[holder.bindingAdapterPosition].book.shelfLocation)
            intent.putExtra(BookDetailsActivity.DESCRIPTION_KEY, data[holder.bindingAdapterPosition].book.description)
            intent.putExtra(BookDetailsActivity.PAGES_KEY, data[holder.bindingAdapterPosition].book.pageCount)
            holder.itemView.context.startActivity(intent)
        })
    }

    fun addTransactions(newTransactions: ArrayList<TransactionModel>) {
        val startingIndex = data.size
        data.addAll(newTransactions)
        notifyItemRangeInserted(startingIndex, newTransactions.size)
    }

    fun removeAllTransactions() {
        data.clear()
        this.notifyDataSetChanged()
    }
}