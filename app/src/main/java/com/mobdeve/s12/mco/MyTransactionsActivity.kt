package com.mobdeve.s12.mco

import android.content.res.ColorStateList
import android.graphics.Typeface
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.mobdeve.s12.mco.databinding.ActivityMyTransactionsBinding
import com.mobdeve.s12.mco.databinding.ComponentMytransSortDialogBinding

class MyTransactionsActivity : AppCompatActivity() {
    companion object {
        private const val VERTICAL_SPACE = 24
    }

    private enum class SortOption {
        TITLE,
        AUTHOR,
        NEWEST,
        OLDEST,
    }

    private lateinit var myTransactionsBinding : ActivityMyTransactionsBinding
    private lateinit var filterButtons : List<Button>
    private var activeFilterBtn : Button? = null

    private var sortDialogBinding : ComponentMytransSortDialogBinding? = null
    private var sortDialogOptionButtons : List<Pair<SortOption, Button>>? = null
    private var activeSortOption : SortOption = SortOption.TITLE
    private var tempSortOption : SortOption? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        myTransactionsBinding = ActivityMyTransactionsBinding.inflate(layoutInflater)
        setContentView(myTransactionsBinding.root)

        initFilterButtons()

        myTransactionsBinding.mytransIbSortbtn.setOnClickListener {
            showSortDialog()
        }

        myTransactionsBinding.mytransRvTransactions.adapter = MyTransactionsTransAdapter(BookGenerator.generateSampleBooks())
        myTransactionsBinding.mytransRvTransactions.layoutManager = LinearLayoutManager(this)
        myTransactionsBinding.mytransRvTransactions.addItemDecoration(MarginItemDecoration(resources.displayMetrics, VERTICAL_SPACE))
    }

    private fun initFilterButtons() {
        filterButtons = listOf(
            myTransactionsBinding.mytransBtnFiltermatchall,
            myTransactionsBinding.mytransBtnFiltermatchpending,
            myTransactionsBinding.mytransBtnFiltermatchpickedup,
            myTransactionsBinding.mytransBtnFiltermatchreturned,
            myTransactionsBinding.mytransBtnFiltermatchcancelled,
            myTransactionsBinding.mytransBtnFiltermatchpickupmissed,
            myTransactionsBinding.mytransBtnFiltermatchreturnmissed
        )

        activeFilterBtn = myTransactionsBinding.mytransBtnFiltermatchall
        filterButtons.forEach { button ->
            button.setOnClickListener {
                activeFilterBtn?.let {
                    it.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.search_filter_button))
                    button.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.main_green))
                    activeFilterBtn = button
                }
            }
        }
    }

    private fun showSortDialog() {
        val bottomSheetDialog = BottomSheetDialog(this)
        sortDialogBinding = ComponentMytransSortDialogBinding.inflate(layoutInflater)
        bottomSheetDialog.setContentView(sortDialogBinding!!.root)

        sortDialogOptionButtons = listOf(
            SortOption.TITLE to sortDialogBinding!!.dialogMytransBtnFiltertitle,
            SortOption.AUTHOR to sortDialogBinding!!.dialogMytransBtnFilterauthor,
            SortOption.NEWEST to sortDialogBinding!!.dialogMytransBtnFilternewest,
            SortOption.OLDEST to sortDialogBinding!!.dialogMytransBtnFilteroldest
        )

        sortDialogOptionButtons!!.forEach { (option, button) ->
            button.setOnClickListener {
                // highlight the selected sort option but don't save it yet (user must confirm)
                tempSortOption = option
                highlightSortOption()
            }
        }

        bottomSheetDialog.setOnDismissListener {
            sortDialogBinding = null
            sortDialogOptionButtons = null
            tempSortOption = null
        }

        sortDialogBinding!!.dialogMytransBtnConfirmbtn.setOnClickListener {
            // save selected sorting option after user hits confirm button
            tempSortOption?.let {
                activeSortOption = it
            }
            bottomSheetDialog.dismiss()
        }

        // highlight last selected sorting option
        highlightSortOption()

        bottomSheetDialog.show()
    }

    private fun highlightSortOption() {
        sortDialogOptionButtons?.forEach { (option, button) ->
            // highlight newly selected sorting option, otherwise highlight default/last chosen sort option
            val isBold = option == tempSortOption || (tempSortOption == null && option == activeSortOption)
            button.setTypeface(null, if (isBold) Typeface.BOLD else Typeface.NORMAL)
        }
    }
}