package com.mobdeve.s12.mco

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.mobdeve.s12.mco.databinding.ActivityAdminTransactionsBinding
import com.mobdeve.s12.mco.databinding.ComponentDialogAdminTransBinding

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
        initLogoutButton()
        initFilterButton()
    }

    private fun setRVRecyclerView() {
        rvRecyclerView = adminTransBinding.adminTransRv
        rvAdapter = AdminTransactionsAdapter(ArrayList(transactions))
        rvRecyclerView.adapter = rvAdapter
        rvRecyclerView.layoutManager = LinearLayoutManager(this)
        rvRecyclerView.addItemDecoration(MarginItemDecoration(resources.displayMetrics, VERTICAL_SPACE))
    }

    private fun initLogoutButton() {
        adminTransBinding.adminTransBtnLogout.setOnClickListener {
            // end activity which Login started
            finish()
        }
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