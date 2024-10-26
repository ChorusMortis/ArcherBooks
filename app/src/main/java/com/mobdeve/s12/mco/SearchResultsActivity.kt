package com.mobdeve.s12.mco

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.mobdeve.s12.mco.databinding.ActivitySearchResultsBinding
import com.mobdeve.s12.mco.databinding.DialogSearchresultsSortDialogBinding

class SearchResultsActivity : AppCompatActivity() {
    companion object {
        private const val VERTICAL_SPACE = 25
    }

    private enum class SortOption {
        RELEVANCE,
        NEWEST,
        OLDEST,
    }

    private lateinit var searchResultsBinding : ActivitySearchResultsBinding
    private lateinit var searchFilterButtons : List<Button>
    private var activeSearchFilterBtn : Button? = null

    private var sortDialogBinding : DialogSearchresultsSortDialogBinding? = null
    private var sortDialogOptionButtons : List<Pair<SortOption, Button>>? = null
    private var activeSortOption : SortOption = SortOption.RELEVANCE
    private var tempSortOption : SortOption? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        searchResultsBinding = ActivitySearchResultsBinding.inflate(layoutInflater)
        setContentView(searchResultsBinding.root)

        initSearchFilterButtons()

        searchResultsBinding.searchresultsIbSortbtn.setOnClickListener {
            showSortResultsDialog()
        }

        searchResultsBinding.searchresultsRvResults.adapter = SearchResultsResultsAdapter(BookGenerator.generateSampleBooks())
        searchResultsBinding.searchresultsRvResults.layoutManager = LinearLayoutManager(this)
        searchResultsBinding.searchresultsRvResults.addItemDecoration(MarginItemDecoration(VERTICAL_SPACE))
    }

    private fun initSearchFilterButtons() {
        searchFilterButtons = listOf<Button>(
            searchResultsBinding.searchresultsBtnFiltermatchall,
            searchResultsBinding.searchresultsBtnFiltermatchtitle,
            searchResultsBinding.searchresultsBtnFiltermatchauthor,
            searchResultsBinding.searchresultsBtnFiltermatchsubject,
            searchResultsBinding.searchresultsBtnFiltermatchpublisher
        )

        activeSearchFilterBtn = searchResultsBinding.searchresultsBtnFiltermatchall
        searchFilterButtons.forEach { button ->
            button.setOnClickListener {
                activeSearchFilterBtn?.let {
                    it.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#C3C3C3"))
                    button.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#61AA63"))
                    activeSearchFilterBtn = button
                }
            }
        }
    }

    private fun showSortResultsDialog() {
        val bottomSheetDialog = BottomSheetDialog(this)
        sortDialogBinding = DialogSearchresultsSortDialogBinding.inflate(layoutInflater)
        bottomSheetDialog.setContentView(sortDialogBinding!!.root)

        sortDialogOptionButtons = listOf(
            SortOption.RELEVANCE to sortDialogBinding!!.dialogSearchresultsBtnFilterrevelance,
            SortOption.NEWEST to sortDialogBinding!!.dialogSearchresultsBtnFilternewest,
            SortOption.OLDEST to sortDialogBinding!!.dialogSearchresultsBtnFilteroldest
        )

        sortDialogOptionButtons!!.forEach { (option, button) ->
            button.setOnClickListener {
                // highlight the selected sort option but don't save it
                tempSortOption = option
                highlightSortOption()
            }
        }

        bottomSheetDialog.setOnDismissListener {
            sortDialogBinding = null
            sortDialogOptionButtons = null
            tempSortOption = null
        }

        sortDialogBinding!!.dialogSearchresultsBtnConfirmbtn.setOnClickListener {
            // if user has selected a new sorting option, save it
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
        // highlight chosen sort option immediately
        tempSortOption?.let {
            sortDialogOptionButtons?.forEach { (option, button) ->
                if (option == tempSortOption) {
                    button.setTypeface(null, Typeface.BOLD)
                } else {
                    button.setTypeface(null, Typeface.NORMAL)
                }
            }
            // terminate since chosen sort option is not saved until user confirms
            return
        }

        // highlight default/last chosen sort option
        sortDialogOptionButtons?.forEach { (option, button) ->
            if (option == activeSortOption) {
                button.setTypeface(null, Typeface.BOLD)
            } else {
                button.setTypeface(null, Typeface.NORMAL)
            }
        }
    }
}