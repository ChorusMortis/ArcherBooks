package com.mobdeve.s12.mco

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mobdeve.s12.mco.databinding.ComponentDialogAdmintransEditstatusBinding
import com.mobdeve.s12.mco.databinding.ItemAdminTsCardBinding

class AdminTransactionsAdapter(private val data: ArrayList<TransactionModel>): RecyclerView.Adapter<AdminTransactionsViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdminTransactionsViewHolder {
        val itemAdminTransBinding = ItemAdminTsCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val adminTransViewHolder = AdminTransactionsViewHolder(itemAdminTransBinding)

        itemAdminTransBinding.itemAdminEditBtn.setOnClickListener {
            showEditDialog(parent.context)
        }

        return adminTransViewHolder
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: AdminTransactionsViewHolder, position: Int) {
        holder.bindData(data[position])
    }

    private fun showEditDialog(context: Context) {
        val editDialogBinding = ComponentDialogAdmintransEditstatusBinding.inflate(LayoutInflater.from(context))
        val dialog = AlertDialog.Builder(context, R.style.WrapContentDialog)
            .setView(editDialogBinding.root)
            .setCancelable(true)
            .create()

        // make background transparent so dialog floats
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // TODO: change message based on transaction status

        editDialogBinding.dialogAdmintransBtnCancel.setOnClickListener {
            dialog.dismiss()
        }

        editDialogBinding.dialogAdmintransBtnConfirm.setOnClickListener {
            // TODO: Change transaction status after hitting confirm
            // TODO: Change edit button after book is returned
            dialog.dismiss()
        }

        dialog.show()
    }
}
