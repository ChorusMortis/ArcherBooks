package com.mobdeve.s12.mco

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.mobdeve.s12.mco.databinding.ComponentDialogSearchBinding
import com.mobdeve.s12.mco.databinding.FragmentSearchBinding

class SearchResultsFragment : Fragment() {
    companion object {
        private const val VERTICAL_SPACE = 12

        const val SEARCH_RESULTS_SORT_PREF = "SEARCH_RESULTS_SORT_PREF"
        const val SEARCH_RESULTS_FILTER_PREF = "SEARCH_RESULTS_FILTER_PREF"
    }

    enum class SortOption {
        RELEVANCE,
        NEWEST,
    }

    enum class FilterOption {
        ALL,
        TITLE,
        AUTHOR,
        SUBJECT,
        PUBLISHER,
    }

    private lateinit var searchResultsBinding : FragmentSearchBinding

    private var sortDialogBinding : ComponentDialogSearchBinding? = null

    private var sortDialogOptionButtons : List<Pair<SortOption, Button>>? = null
    private var activeSortOption : SortOption = SortOption.RELEVANCE
    private var tempActiveSortOption : SortOption? = null

    private var searchFilterButtons : List<Pair<FilterOption, Button>>? = null
    private var activeSearchFilterOption : FilterOption = FilterOption.ALL
    private var tempActiveSearchFilterOption : FilterOption? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        searchResultsBinding = FragmentSearchBinding.inflate(inflater, container, false)

//        // reset sorting to relevance and filtering to all when search is done
//        searchResultsBinding.searchresultsIbSearchbtn.setOnClickListener {
//            activeSortOption = SortOption.RELEVANCE
//            activeSearchFilterOption = FilterOption.ALL
//        }

        initPreferences()

        searchResultsBinding.searchBtnFilterSort.setOnClickListener {
            showSortResultsDialog()
        }

        searchResultsBinding.searchRvResults.adapter = SearchResultsResultsAdapter(BookGenerator.generateSampleBooks())
        searchResultsBinding.searchRvResults.layoutManager = GridLayoutManager(activity, 2)
        searchResultsBinding.searchRvResults.addItemDecoration(MarginItemDecoration(resources.displayMetrics, VERTICAL_SPACE))

        return searchResultsBinding.root
    }

    private fun initPreferences() {
        val sp = PreferenceManager.getDefaultSharedPreferences(activity)

        val strPrefSortOption = sp.getString(SEARCH_RESULTS_SORT_PREF, "")
        val strPrefFilterOption = sp.getString(SEARCH_RESULTS_FILTER_PREF, "")

        val prefSortOption = SortOption.entries.firstOrNull { it.name == strPrefSortOption }
        val prefFilterOption = FilterOption.entries.firstOrNull { it.name == strPrefFilterOption }

        prefSortOption?.let {
            activeSortOption = it
        }

        prefFilterOption?.let {
            activeSearchFilterOption = it
        }
    }

    private fun showSortResultsDialog() {
        val bottomSheetDialog = BottomSheetDialog(requireActivity(), R.style.BottomSheetDialog)
        sortDialogBinding = ComponentDialogSearchBinding.inflate(layoutInflater)
        bottomSheetDialog.setContentView(sortDialogBinding!!.root)

        bottomSheetDialog.setOnDismissListener {
            sortDialogBinding = null

            sortDialogOptionButtons = null
            tempActiveSortOption = null

            searchFilterButtons = null
            tempActiveSearchFilterOption = null
        }

        sortDialogBinding!!.dialogSearchresultsBtnConfirmbtn.setOnClickListener {
            // save selected sorting option after user hits confirm button
            tempActiveSortOption?.let {
                activeSortOption = it

                val sp = PreferenceManager.getDefaultSharedPreferences(activity)
                val editor = sp.edit()
                editor.putString(SEARCH_RESULTS_SORT_PREF, it.name)
                editor.apply()
            }

            // save selected filter option after user hits confirm button
            tempActiveSearchFilterOption?.let {
                activeSearchFilterOption = it

                val sp = PreferenceManager.getDefaultSharedPreferences(activity)
                val editor = sp.edit()
                editor.putString(SEARCH_RESULTS_FILTER_PREF, it.name)
                editor.apply()
            }

            bottomSheetDialog.dismiss()
        }

        // initialize sorting option buttons and highlight the active one
        initSortOptionButtons()

        // initialize search filter buttons and highlight the active one
        initSearchFilterButtons()

        bottomSheetDialog.show()
    }

    private fun initSortOptionButtons() {
        sortDialogOptionButtons = listOf(
            SortOption.RELEVANCE to sortDialogBinding!!.dialogSearchresultsBtnSortRelevance,
            SortOption.NEWEST to sortDialogBinding!!.dialogSearchresultsBtnSortNewest,
        )

        sortDialogOptionButtons!!.forEach { (option, button) ->
            button.setOnClickListener {
                // highlight the selected sort option but don't save it yet (user must confirm)
                tempActiveSortOption = option
                highlightSortOption()
            }
        }

        // highlight last selected sorting option
        highlightSortOption()
    }

    private fun initSearchFilterButtons() {
        searchFilterButtons = sortDialogBinding?.let {
            listOf(
                FilterOption.ALL to it.dialogSearchresultsBtnFilterAll,
                FilterOption.TITLE to it.dialogSearchresultsBtnFilterTitle,
                FilterOption.AUTHOR to it.dialogSearchresultsBtnFilterAuthor,
                FilterOption.SUBJECT to it.dialogSearchresultsBtnFilterSubject,
                FilterOption.PUBLISHER to it.dialogSearchresultsBtnFilterPublisher
            )
        }

        searchFilterButtons!!.forEach { (option, button) ->
            button.setOnClickListener {
                // highlight the selected filter option but don't save it yet (user must confirm)
                tempActiveSearchFilterOption = option
                highlightSearchFilterButton()
            }
        }

        highlightSearchFilterButton()
    }

    private fun highlightSortOption() {
        sortDialogOptionButtons?.forEach { (option, button) ->
            // highlight newly selected sorting option, otherwise highlight default/last chosen sort option
            // unhighlight the rest of the sorting options

            val isHighlighted = option == tempActiveSortOption || (tempActiveSortOption == null && option == activeSortOption)

            button.backgroundTintList = if (isHighlighted) ColorStateList.valueOf(ContextCompat.getColor(requireActivity(), R.color.main_green)) else null
            button.setTextColor(if (isHighlighted) Color.parseColor("#FFFFFF") else ContextCompat.getColor(requireActivity(), R.color.dark_background))
        }
    }

    private fun highlightSearchFilterButton() {
        searchFilterButtons?.forEach { (option, button) ->
            activeSearchFilterOption.let {
                // highlight newly selected filter option, otherwise highlight default/last chosen filter option
                // unhighlight the rest of the filter options
                val isHighlighted = option == tempActiveSearchFilterOption || (tempActiveSearchFilterOption == null && option == activeSearchFilterOption)

                button.backgroundTintList = if (isHighlighted) ColorStateList.valueOf(ContextCompat.getColor(requireActivity(), R.color.main_green)) else null
                button.setTextColor(if (isHighlighted) Color.parseColor("#FFFFFF") else ContextCompat.getColor(requireActivity(), R.color.dark_background))
            }
        }
    }
}