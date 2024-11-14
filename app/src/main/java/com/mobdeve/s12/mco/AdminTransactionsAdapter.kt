package com.mobdeve.s12.mco

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mobdeve.s12.mco.databinding.ItemAdminTsCardBinding

class AdminTransactionsAdapter(private val data: ArrayList<TransactionModel>): RecyclerView.Adapter<AdminTransactionsViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdminTransactionsViewHolder {
        val itemAdminTransBinding = ItemAdminTsCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val adminTransViewHolder = AdminTransactionsViewHolder(itemAdminTransBinding)

        return adminTransViewHolder
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: AdminTransactionsViewHolder, position: Int) {
        holder.bindData(data[position])
    }
}
