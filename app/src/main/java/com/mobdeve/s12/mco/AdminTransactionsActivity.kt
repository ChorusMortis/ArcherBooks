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
import android.widget.Button
import android.widget.PopupMenu
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.mobdeve.s12.mco.databinding.ActivityAdminTransactionsBinding
import com.mobdeve.s12.mco.databinding.ComponentDialogAdminTransBinding
import com.mobdeve.s12.mco.databinding.ComponentDialogConfirmlogoutBinding

class AdminTransactionsActivity : AppCompatActivity() {
    companion object {
        private const val VERTICAL_SPACE = 12
    }

    private enum class FilterOption {
        ALL,
        TO_PICKUP,
        TO_RETURN,
        RETURNED,
        CANCELLED,
    }

    private lateinit var adminTransBinding : ActivityAdminTransactionsBinding
    private lateinit var rvRecyclerView: RecyclerView
    private lateinit var rvAdapter: AdminTransactionsAdapter

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

        adminTransBinding.adminTransBtnOptions.setOnClickListener {
            showPopupMenu()
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
        rvAdapter = AdminTransactionsAdapter(ArrayList(transactions))
        rvRecyclerView.adapter = rvAdapter
        rvRecyclerView.layoutManager = LinearLayoutManager(this)
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
                FilterOption.TO_PICKUP to it.dialogAtBtnFilterForpickup,
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