package com.mobdeve.s12.mco

import android.app.Activity
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
import java.text.SimpleDateFormat
import java.util.Locale

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

        addConfirmBtnListener(context, editDialogBinding, dialog, position)

        dialog.show()
    }

    private fun addConfirmBtnListener(context: Context, editDialogBinding : ComponentDialogAdmintransEditstatusBinding, dialog: AlertDialog, position: Int) {
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

                    // cancel pickup reminder notification regardless of whether user is logged in or not
                    val bookTitle = data[position].book.title
                    val transactionId = data[position].transactionId
                    val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH)
                    val pickupDateString = outputFormat.format(data[position].expectedPickupDate.toDate())
                    val bookPickupReminder = "Reminder to pick up by $pickupDateString"
                    val pickupNotifId = "{$transactionId}_pickup"
                    NotificationReceiver.cancelNotification(context as Activity, "Pick up $bookTitle", bookPickupReminder, pickupNotifId, pickupNotifId)
                } else if(oldStatus == TransactionModel.Status.TO_RETURN) {
                    changeToReturnStatusToReturnedCallback()
                    firestoreHandler.updateTransaction(data[position].transactionId, "actualReturnDate", currentTime)
                    data[position].actualReturnDate = currentTime

                    // cancel return reminder notification regardless of whether user is logged in or not
                    val bookTitle = data[position].book.title
                    val transactionId = data[position].transactionId
                    val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH)
                    val returnDateString = outputFormat.format(data[position].expectedReturnDate.toDate())
                    val bookReturnReminder = "Reminder to return by $returnDateString"
                    val returnNotifId = "{$transactionId}_return"
                    NotificationReceiver.cancelNotification(context as Activity, "Return $bookTitle", bookReturnReminder, returnNotifId, returnNotifId)

                    // send thank you notification regardless of whether user is logged in or not
                    val bookReturnedMsg = "Thank you for using Archer Books!"
                    val bookReturnedNotifId = "${transactionId}_returned"
                    NotificationReceiver.sendNotification(context, "Returned $bookTitle", bookReturnedMsg, bookReturnedNotifId, bookReturnedNotifId, 3 * 1000)
                } else if(oldStatus == TransactionModel.Status.OVERDUE) {
                    changeOverdueStatusToReturnedCallback()
                    firestoreHandler.updateTransaction(data[position].transactionId, "actualReturnDate", currentTime)
                    data[position].actualReturnDate = currentTime

                    // send clearance hold notification regardless of whether user is logged in or not
                    val bookTitle = data[position].book.title
                    val transactionId = data[position].transactionId
                    val returnedOverdueMsg = "You can now borrow more books and your clearance hold has been lifted."
                    val bookReturnedOverdueNotifId = "${transactionId}_returnedoverdue"
                    NotificationReceiver.sendNotification(context as Activity, "Returned $bookTitle", returnedOverdueMsg, bookReturnedOverdueNotifId, bookReturnedOverdueNotifId, 3 * 1000)
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
