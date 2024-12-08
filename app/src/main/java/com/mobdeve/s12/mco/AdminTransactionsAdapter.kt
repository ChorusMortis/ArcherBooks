package com.mobdeve.s12.mco

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.mobdeve.s12.mco.databinding.ComponentDialogAdmintransEditstatusBinding
import com.mobdeve.s12.mco.databinding.ItemAdminTsCardBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AdminTransactionsAdapter(private val data: ArrayList<TransactionModel>,
                               private val changeStatusToToReturnCallback : () -> Unit,
                               private val changeToReturnStatusToReturnedCallback: () -> Unit,
                               private val changeOverdueStatusToReturnedCallback: () -> Unit): RecyclerView.Adapter<AdminTransactionsViewHolder>() {
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

        addConfirmBtnListener(editDialogBinding, dialog, position)

        dialog.show()
    }

    private fun addConfirmBtnListener(editDialogBinding : ComponentDialogAdmintransEditstatusBinding, dialog: AlertDialog, position: Int) {
        editDialogBinding.dialogAdmintransBtnConfirm.setOnClickListener {
            var newStatus : TransactionModel.Status = TransactionModel.Status.RETURNED
            if(data[position].status == TransactionModel.Status.FOR_PICKUP) {
                newStatus = TransactionModel.Status.TO_RETURN
            } else if(data[position].status == TransactionModel.Status.TO_RETURN ||
                      data[position].status == TransactionModel.Status.OVERDUE) {
                newStatus = TransactionModel.Status.RETURNED
            }

            val firestoreHandler = FirestoreHandler.getInstance(editDialogBinding.root.context)
            CoroutineScope(Dispatchers.Main).launch {
                firestoreHandler.updateTransaction(data[position].transactionId, "status", newStatus.toString())
                val oldStatus = data[position].status
                val currentTime = Timestamp.now()

                if(oldStatus == TransactionModel.Status.FOR_PICKUP) {
                    changeStatusToToReturnCallback()
                    firestoreHandler.updateTransaction(data[position].transactionId, "actualPickupDate", currentTime)
                    data[position].actualPickupDate = currentTime
                } else if(oldStatus == TransactionModel.Status.TO_RETURN) {
                    changeToReturnStatusToReturnedCallback()
                    firestoreHandler.updateTransaction(data[position].transactionId, "actualReturnDate", currentTime)
                    data[position].actualReturnDate = currentTime
                } else if(oldStatus == TransactionModel.Status.OVERDUE) {
                    changeOverdueStatusToReturnedCallback()
                    firestoreHandler.updateTransaction(data[position].transactionId, "actualReturnDate", currentTime)
                    data[position].actualReturnDate = currentTime
                }

                data[position].status = newStatus
                dialog.dismiss()
                this@AdminTransactionsAdapter.notifyItemChanged(position)
            }
        }
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
