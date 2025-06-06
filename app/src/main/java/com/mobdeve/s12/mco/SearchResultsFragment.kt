package com.mobdeve.s12.mco

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.mobdeve.s12.mco.databinding.ComponentDialogSearchBinding
import com.mobdeve.s12.mco.databinding.FragmentSearchBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Integer.min


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

    private lateinit var rvAdapter: SearchResultsResultsAdapter
    private lateinit var gridLayoutManager: GridLayoutManager
    private var searchStartingIndex = 0
    private var isLoading = false
    private var hasMoreData = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        searchResultsBinding = FragmentSearchBinding.inflate(inflater, container, false)

        initPreferences()

        searchResultsBinding.searchBtnFilterSort.setOnClickListener {
            showSortResultsDialog()
        }

        addListenerSearchBtn()

        rvAdapter = SearchResultsResultsAdapter(arrayListOf())
        searchResultsBinding.searchRvResults.adapter = rvAdapter
        gridLayoutManager = GridLayoutManager(activity, 2)
        searchResultsBinding.searchRvResults.layoutManager = gridLayoutManager
        searchResultsBinding.searchRvResults.addItemDecoration(MarginItemDecoration(resources.displayMetrics, VERTICAL_SPACE))

        addRVScrollListener()

        return searchResultsBinding.root
    }

    override fun onStart() {
        super.onStart()

        // request focus and show keyboard in onStart since layout might not be fully inflated when done in onCreate
        searchResultsBinding.searchEtSearchBar.requestFocus()
        val inputMethodManager = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(searchResultsBinding.searchEtSearchBar, InputMethodManager.SHOW_IMPLICIT)
    }

    override fun onResume() {
        super.onResume()
        // reset sorting to relevance and filtering to all when search is done
        rvAdapter.notifyDataSetChanged()
        activeSortOption = SortOption.RELEVANCE
        activeSearchFilterOption = FilterOption.ALL
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

    private fun addListenerSearchBtn() {
        searchResultsBinding.searchEtSearchBar.setOnEditorActionListener{ view, actionId, event ->
            if(actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_GO ||
                actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_NEXT ||
                actionId == EditorInfo.IME_NULL) {

               initialSearch(view)
                true

            } else {
                false
            }
        }
    }

    private fun initialSearch(view: View) {
        isLoading = true
        searchResultsBinding.initialSearchProgressBar.visibility = View.VISIBLE
        searchResultsBinding.searchTvNoresultsMessage.visibility = View.GONE
        rvAdapter.removeAllBooks()

        searchStartingIndex = 0 // reset book starting index every time search is first triggered
        val searchQuery = searchResultsBinding.searchEtSearchBar.text.toString()
        val googleBooksAPIHandler = GoogleBooksAPIHandler()

        CoroutineScope(Dispatchers.Main).launch {
            val retrievedBooks = googleBooksAPIHandler.getBooks(searchQuery, activeSortOption, activeSearchFilterOption, searchStartingIndex, 20)
            Log.d("SearchResultsFragment", "Retrieved number of books from GoogleBooksAPIHandler = ${retrievedBooks?.size}")
            rvAdapter.removeAllBooks()
            rvAdapter.addBooks(retrievedBooks!!)
            hideKeyboard(view)
            searchStartingIndex += min(retrievedBooks.size, 20)
            isLoading = false
            searchResultsBinding.initialSearchProgressBar.visibility = View.GONE
            setNoResultsMsgVisibility(retrievedBooks)
        }
    }

    private fun repeatedSearch(view: View) {
        isLoading = true
        searchResultsBinding.scrollSearchProgressBar.visibility = View.VISIBLE
        val layoutParams = searchResultsBinding.searchRvResults.layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.bottomMargin = 40
        val searchQuery = searchResultsBinding.searchEtSearchBar.text.toString()
        val googleBooksAPIHandler = GoogleBooksAPIHandler()

        CoroutineScope(Dispatchers.Main).launch {
            val retrievedBooks = googleBooksAPIHandler.getBooks(searchQuery, activeSortOption, activeSearchFilterOption, searchStartingIndex, 20)
            Log.d("SearchResultsFragment", "Retrieved number of books from GoogleBooksAPIHandler = ${retrievedBooks?.size}")
            if(!retrievedBooks.isNullOrEmpty()) {
                rvAdapter.addBooks(retrievedBooks)
                searchStartingIndex += min(retrievedBooks.size, 20)
            } else {
                hasMoreData = false
                setNoResultsMsgVisibility(retrievedBooks)
            }
            isLoading = false
            searchResultsBinding.scrollSearchProgressBar.visibility = View.GONE
            layoutParams.bottomMargin = 0
            hideKeyboard(view)
        }
    }

    private fun hideKeyboard(view: View) {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun addRVScrollListener() {
        searchResultsBinding.searchRvResults.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val visibleItemCount = gridLayoutManager.childCount
                val totalItemCount = gridLayoutManager.itemCount
                val firstVisibleItemPosition = gridLayoutManager.findFirstVisibleItemPosition()

                if(!isLoading && hasMoreData) {
                    if(visibleItemCount + firstVisibleItemPosition >= totalItemCount
                        && firstVisibleItemPosition >= 0
                        && totalItemCount >= 20) {
                        repeatedSearch(requireView())
                    }
                }
            }
        })
    }

    private fun setNoResultsMsgVisibility(results: List<BookModel>?) {
        val visibility = if (results.isNullOrEmpty()) View.VISIBLE else View.GONE
        searchResultsBinding.searchTvNoresultsMessage.visibility = visibility
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

            initialSearch(this.requireView())
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