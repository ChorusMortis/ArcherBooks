package com.mobdeve.s12.mco

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.PopupMenu
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.firestore.DocumentSnapshot
import com.mobdeve.s12.mco.TransactionsFragment.Companion
import com.mobdeve.s12.mco.databinding.ActivityAdminTransactionsBinding
import com.mobdeve.s12.mco.databinding.ComponentDialogAdminTransBinding
import com.mobdeve.s12.mco.databinding.ComponentDialogConfirmlogoutBinding
import com.mobdeve.s12.mco.databinding.FragmentTransactionsBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AdminTransactionsActivity : AppCompatActivity() {
    companion object {
        private const val VERTICAL_SPACE = 12
    }

    enum class FilterOption {
        ALL,
        FOR_PICKUP,
        TO_RETURN,
        RETURNED,
        CANCELLED,
    }

    private var transactionsObjList : ArrayList<TransactionModel> = arrayListOf()
    private var displayIncrement = 8L
    private var isLoading = false
    private var hasMoreData = true
    private var lastTransactionRetrieved : DocumentSnapshot? = null
    private var transactionsCount = 0

    private lateinit var adminTransBinding : ActivityAdminTransactionsBinding
    private lateinit var rvRecyclerView: RecyclerView
    private lateinit var rvAdapter: AdminTransactionsAdapter
    private lateinit var layoutManager: LinearLayoutManager

    private var dialogBinding : ComponentDialogAdminTransBinding? = null

    private var filterButtons : List<Pair<FilterOption, Button>>? = null
    private var activeFilterOption : FilterOption = FilterOption.ALL
    private var tempActiveFilterOption : FilterOption? = null

    private val books = BookGenerator.generateSampleBooks()
    private val users = UserGenerator.generateSampleUsers()
    private val transactions = TransactionGenerator.generateSampleTransactions(books, users)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adminTransBinding = ActivityAdminTransactionsBinding.inflate(layoutInflater)
        setContentView(adminTransBinding.root)

        setRVRecyclerView()
        initFilterButton()

        passInitialData()
        addRVScrollListener()


        adminTransBinding.adminTransBtnOptions.setOnClickListener {
            showPopupMenu()
        }
    }

    private fun passInitialData() {
        adminTransBinding.adminTransTvEmptyList.visibility = View.GONE
        setTransactionsCount()
        CoroutineScope(Dispatchers.Main).launch {
            lastTransactionRetrieved = null
            isLoading = true
            rvAdapter.removeAllTransactions()
            adminTransBinding.adminTransInitialProgressBar.visibility = View.VISIBLE
            val firestoreHandler = FirestoreHandler.getInstance(this@AdminTransactionsActivity)

            val returnedObjList = firestoreHandler.getTransactionsForAdmin(this@AdminTransactionsActivity.displayIncrement, activeFilterOption, lastTransactionRetrieved)
            addDataToAdapter(returnedObjList)

            if(returnedObjList?.first?.size == 0) {
                adminTransBinding.adminTransTvEmptyList.visibility = View.VISIBLE
            }

            adminTransBinding.adminTransInitialProgressBar.visibility = View.GONE
            isLoading = false
        }
    }

    private fun setTransactionsCount() {
        val firestoreHandler = FirestoreHandler.getInstance(this)
        adminTransBinding.adminTransPbTotal.visibility = View.VISIBLE
        adminTransBinding.adminTransPbFp.visibility = View.VISIBLE
        adminTransBinding.adminTransPbTr.visibility = View.VISIBLE
        adminTransBinding.adminTransPbO.visibility = View.VISIBLE
        adminTransBinding.adminTransPbReturned.visibility = View.VISIBLE

        CoroutineScope(Dispatchers.Main).launch {
            val details = firestoreHandler.getAdminTransactionsDetails()
            val totalActiveCount = details["forPickup"]!! + details["toReturn"]!! + details["overdue"]!!
            var label = " Transactions"
            if(totalActiveCount == 1L) {
                label = " Transaction"
            }

            adminTransBinding.adminTransTvTotalValue.text = totalActiveCount.toString() + label
            adminTransBinding.adminTransPbTotal.visibility = View.GONE
            adminTransBinding.adminTransTvFpValue.text = details["forPickup"]!!.toString()
            adminTransBinding.adminTransPbFp.visibility = View.GONE
            adminTransBinding.adminTransTvTrValue.text = details["toReturn"]!!.toString()
            adminTransBinding.adminTransPbTr.visibility = View.GONE
            adminTransBinding.adminTransTvOdValue.text = details["overdue"]!!.toString()
            adminTransBinding.adminTransPbO.visibility = View.GONE
            adminTransBinding.adminTransTvRValue.text = details["returned"]!!.toString()
            adminTransBinding.adminTransPbReturned.visibility = View.GONE
        }
    }

    private fun addDataToAdapter(returnedObjList: Pair<ArrayList<TransactionModel>, DocumentSnapshot?>?) {
        if(returnedObjList != null) {
            transactionsObjList = returnedObjList.first
            lastTransactionRetrieved = returnedObjList.second
            transactionsCount = transactionsObjList.size

            Log.d("AdminTransactionsActivity", "Retrieved a total of ${transactionsObjList.size} transactions")

            if(lastTransactionRetrieved == null) {
                hasMoreData = false
            }

            rvAdapter.addTransactions(transactionsObjList)
        }
    }

    private fun addRVScrollListener() {
        adminTransBinding.adminTransRv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                if(!isLoading && hasMoreData) {
                    if(visibleItemCount + firstVisibleItemPosition >= totalItemCount
                        && firstVisibleItemPosition >= 0
                        && totalItemCount >= displayIncrement) {
                        incrementData()
                    }
                }
            }
        })
    }

    private fun incrementData() {
        isLoading = true
        adminTransBinding.adminTransScrollProgressBar.visibility = View.VISIBLE
        val layoutParams = adminTransBinding.adminTransRv.layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.bottomMargin = 180

        val firestoreHandler = FirestoreHandler.getInstance(this)

        CoroutineScope(Dispatchers.Main).launch {
            val returnedObjList = firestoreHandler.getTransactionsForAdmin(this@AdminTransactionsActivity.displayIncrement, activeFilterOption, lastTransactionRetrieved)
            addDataToAdapter(returnedObjList)
            adminTransBinding.adminTransScrollProgressBar.visibility = View.GONE
            layoutParams.bottomMargin = 0
            isLoading = false
        }
    }

    private fun refreshData() {
        adminTransBinding.adminTransTvEmptyList.visibility = View.GONE
        lastTransactionRetrieved = null
        hasMoreData = true
        CoroutineScope(Dispatchers.Main).launch {
            isLoading = true
            rvAdapter.removeAllTransactions()
            adminTransBinding.adminTransInitialProgressBar.visibility = View.VISIBLE
            val firestoreHandler = FirestoreHandler.getInstance(this@AdminTransactionsActivity)

            val returnedObjList = firestoreHandler.getTransactionsForAdmin(this@AdminTransactionsActivity.displayIncrement, activeFilterOption, lastTransactionRetrieved)
            addDataToAdapter(returnedObjList)

            if(returnedObjList?.first?.size == 0) {
                adminTransBinding.adminTransTvEmptyList.visibility = View.VISIBLE
            }

            adminTransBinding.adminTransInitialProgressBar.visibility = View.GONE
            isLoading = false
        }
    }

    private fun showPopupMenu() {
        val wrapper : Context = ContextThemeWrapper(this, R.style.PopupMenuText)
        val popupMenu = PopupMenu(wrapper, adminTransBinding.adminTransPopupAnchor, Gravity.END)
        val inflater = popupMenu.menuInflater
        inflater.inflate(R.menu.admin_options_items, popupMenu.menu)

        try {
            val fieldMPopup = PopupMenu::class.java.getDeclaredField("mPopup")
            fieldMPopup.isAccessible = true
            val mPopup = fieldMPopup.get(popupMenu)
            mPopup.javaClass.getDeclaredMethod("setForceShowIcon", Boolean::class.java).invoke(mPopup, true)
        } catch (e : Exception) {
            Log.e("AdminTransactionsActivity", "Error showing menu icons")
        } finally {
            popupMenu.show()
        }

        initLogoutButton(popupMenu)
    }

    private fun setRVRecyclerView() {
        rvRecyclerView = adminTransBinding.adminTransRv
        rvAdapter = AdminTransactionsAdapter(arrayListOf())
        rvRecyclerView.adapter = rvAdapter
        layoutManager = LinearLayoutManager(this)
        rvRecyclerView.layoutManager = layoutManager
        rvRecyclerView.addItemDecoration(MarginItemDecoration(resources.displayMetrics, VERTICAL_SPACE))
    }

    private fun initLogoutButton(popupMenu: PopupMenu) {
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.admin_logout -> {
                    showConfirmLogoutDialog()
                    true
                }
                else -> super.onOptionsItemSelected(item)
            }
        }
    }

    private fun showConfirmLogoutDialog() {
        val confirmLogoutDialogBinding =
            ComponentDialogConfirmlogoutBinding.inflate(LayoutInflater.from(this))
        // use custom style to force dialog to wrap content and not take up entire screen's width
        val dialog = AlertDialog.Builder(this,  R.style.WrapContentDialog)
            .setView(confirmLogoutDialogBinding.root)
            .setCancelable(false)
            .create()

        // make background transparent so dialog floats
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        confirmLogoutDialogBinding.dialogConfirmlogoutBtnCancel.setOnClickListener {
            dialog.dismiss()
        }

        confirmLogoutDialogBinding.dialogConfirmlogoutBtnConfirm.setOnClickListener {
            dialog.dismiss()
            // finish activity that was started by Login to logout
            finish()
        }

        dialog.show()
    }

    private fun initFilterButton() {
        adminTransBinding.adminTransBtnFilter.setOnClickListener {
            showFilterDialog()
        }
    }

    private fun showFilterDialog() {
        val bottomSheetDialog = BottomSheetDialog(this, R.style.BottomSheetDialog)
        dialogBinding = ComponentDialogAdminTransBinding.inflate(layoutInflater)
        bottomSheetDialog.setContentView(dialogBinding!!.root)

        bottomSheetDialog.setOnDismissListener {
            dialogBinding = null

            filterButtons = null
            tempActiveFilterOption = null
        }

        dialogBinding!!.dialogAtBtnConfirmbtn.setOnClickListener {
            tempActiveFilterOption?.let {
                activeFilterOption = it
            }

            refreshData()

            bottomSheetDialog.dismiss()
        }

        // initialize filter buttons on dialog and highlight the active/default one
        initDialogFilterButtons()

        bottomSheetDialog.show()
    }

    private fun initDialogFilterButtons() {
        filterButtons = dialogBinding?.let {
            listOf(
                FilterOption.ALL to it.dialogAtBtnFilterAll,
                FilterOption.FOR_PICKUP to it.dialogAtBtnFilterForpickup,
                FilterOption.TO_RETURN to it.dialogAtBtnFilterToreturn,
                FilterOption.RETURNED to it.dialogAtBtnFilterReturned,
                FilterOption.CANCELLED to it.dialogAtBtnFilterCancelled
            )
        }

        filterButtons?.forEach { (option, button) ->
            button.setOnClickListener {
                tempActiveFilterOption = option
                highlightFilterButton()
            }
        }

        // highlight default filter button
        highlightFilterButton()
    }

    private fun highlightFilterButton() {
        filterButtons?.forEach { (option, button) ->
            activeFilterOption.let {
                // highlight newly selected filter option, otherwise highlight default/last chosen filter option
                // unhighlight the rest of the filter options
                val isHighlighted = option == tempActiveFilterOption || (tempActiveFilterOption == null && option == activeFilterOption)

                button.backgroundTintList = if (isHighlighted) ColorStateList.valueOf(ContextCompat.getColor(this, R.color.main_green)) else null
                button.setTextColor(if (isHighlighted) Color.parseColor("#FFFFFF") else ContextCompat.getColor(this, R.color.dark_background))
            }
        }
    }
}