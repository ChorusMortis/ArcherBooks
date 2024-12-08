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
            showEditDialog(parent.context, adminTransViewHolder.bindingAdapterPosition)
        }

        return adminTransViewHolder
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: AdminTransactionsViewHolder, position: Int) {
        holder.bindData(data[position])
    }

    private fun showEditDialog(context: Context, position: Int) {
        val editDialogBinding = ComponentDialogAdmintransEditstatusBinding.inflate(LayoutInflater.from(context))
        val dialog = AlertDialog.Builder(context)
            .setView(editDialogBinding.root)
            .setCancelable(true)
            .create()

        // make background transparent so dialog floats
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val editingTransaction = data[position]

        setDialogMessage(editingTransaction, editDialogBinding)

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

    private fun setDialogMessage(transaction: TransactionModel, dialogBinding: ComponentDialogAdmintransEditstatusBinding) {
        val message = when (transaction.status) {
            TransactionModel.Status.FOR_PICKUP -> "Are you sure the borrower has picked up the book?"
            TransactionModel.Status.TO_RETURN -> "Are you sure the borrower has returned the book?"
            TransactionModel.Status.OVERDUE -> "Are you sure the borrower has returned the book?"
            TransactionModel.Status.CANCELLED -> "The borrower has cancelled the transaction."
            TransactionModel.Status.RETURNED -> "The borrower has already returned the book."
        }
        dialogBinding.dialogAdmintransTvMessage.text = message

        val header = when (transaction.status) {
            TransactionModel.Status.FOR_PICKUP -> "Confirm Pickup"
            TransactionModel.Status.TO_RETURN -> "Confirm Return"
            TransactionModel.Status.OVERDUE -> "Confirm Return"
            TransactionModel.Status.CANCELLED -> "Transaction Cancelled"
            TransactionModel.Status.RETURNED -> "Book Returned"
        }

        dialogBinding.dialogAdmintransTvLabel.text = header
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
