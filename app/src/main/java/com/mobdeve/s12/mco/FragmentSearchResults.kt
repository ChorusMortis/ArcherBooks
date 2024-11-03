package com.mobdeve.s12.mco

import android.content.res.ColorStateList
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.mobdeve.s12.mco.databinding.ActivitySearchResultsBinding
import com.mobdeve.s12.mco.databinding.ComponentSearchresultsSortDialogBinding

class FragmentSearchResults : Fragment() {
    companion object {
        private const val VERTICAL_SPACE = 24
    }

    private enum class SortOption {
        RELEVANCE,
        NEWEST,
        OLDEST,
    }

    private lateinit var searchResultsBinding : ActivitySearchResultsBinding
    private lateinit var searchFilterButtons : List<Button>
    private var activeSearchFilterBtn : Button? = null

    private var sortDialogBinding : ComponentSearchresultsSortDialogBinding? = null
    private var sortDialogOptionButtons : List<Pair<SortOption, Button>>? = null
    private var activeSortOption : SortOption = SortOption.RELEVANCE
    private var tempSortOption : SortOption? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
//        super.onCreate(savedInstanceState)
//        searchResultsBinding = ActivitySearchResultsBinding.inflate(layoutInflater)
//        setContentView(searchResultsBinding.root)
        searchResultsBinding = ActivitySearchResultsBinding.inflate(inflater, container, false)

        initSearchFilterButtons()

        searchResultsBinding.searchresultsIbSearchbtn.setOnClickListener {
            activeSortOption = SortOption.RELEVANCE
        }

        searchResultsBinding.searchresultsIbSortbtn.setOnClickListener {
            showSortResultsDialog()
        }


        searchResultsBinding.searchresultsRvResults.adapter = SearchResultsResultsAdapter(BookGenerator.generateSampleBooks())
        searchResultsBinding.searchresultsRvResults.layoutManager = LinearLayoutManager(activity)
        searchResultsBinding.searchresultsRvResults.addItemDecoration(MarginItemDecoration(resources.displayMetrics, VERTICAL_SPACE))

        return searchResultsBinding.root
    }

    private fun initSearchFilterButtons() {
        searchFilterButtons = listOf(
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
                    it.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireActivity(), R.color.search_filter_button))
                    button.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireActivity(), R.color.main_green))
                    activeSearchFilterBtn = button
                }
            }
        }
    }

    private fun showSortResultsDialog() {
        val bottomSheetDialog = BottomSheetDialog(requireActivity())
        sortDialogBinding = ComponentSearchresultsSortDialogBinding.inflate(layoutInflater)
        bottomSheetDialog.setContentView(sortDialogBinding!!.root)

        sortDialogOptionButtons = listOf(
            SortOption.RELEVANCE to sortDialogBinding!!.dialogSearchresultsBtnFilterrevelance,
            SortOption.NEWEST to sortDialogBinding!!.dialogSearchresultsBtnFilternewest,
            SortOption.OLDEST to sortDialogBinding!!.dialogSearchresultsBtnFilteroldest
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

        sortDialogBinding!!.dialogSearchresultsBtnConfirmbtn.setOnClickListener {
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