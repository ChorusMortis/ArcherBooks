package com.mobdeve.s12.mco

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.mobdeve.s12.mco.databinding.ComponentDialogFavsBinding
import com.mobdeve.s12.mco.databinding.FragmentFavoritesBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Integer.min

class FavoritesFragment : Fragment() {
    companion object {
        private const val VERTICAL_SPACE = 12

        const val FAVORITES_SORT_PREF = "FAVORITES_SORT_PREF"
        const val FAVORITES_FILTER_PREF = "FAVORITES_FILTER_PREF"
    }

    private enum class SortOption {
        NEWEST,
        TITLE,
        AUTHOR,
    }

    private enum class FilterOption {
        ALL,
        TO_PICKUP,
        TO_RETURN,
        OVERDUE,
        RETURNED,
        CANCELLED,
    }

    private lateinit var transactionsFragBinding : FragmentFavoritesBinding
    private lateinit var fAdapter: FavoritesFavsAdapter
    private lateinit var layoutManager: LinearLayoutManager

    private var dataStartingIndex = 0
    private var favoritesObjList : ArrayList<BookModel> = arrayListOf()
    private var displayIncrement = 10
    private var isLoading = false
    private var hasMoreData = true

    private var sortDialogBinding : ComponentDialogFavsBinding? = null

    private var sortDialogOptionButtons : List<Pair<SortOption, Button>>? = null
    private var activeSortOption : SortOption = SortOption.NEWEST
    private var tempActiveSortOption : SortOption? = null

    private var filterButtons : List<Pair<FilterOption, Button>>? = null
    private var activeFilterOption : FilterOption = FilterOption.ALL
    private var tempActiveFilterOption : FilterOption? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        transactionsFragBinding = FragmentFavoritesBinding.inflate(inflater, container, false)

        initPreferences()

        transactionsFragBinding.favoritesBtnSort.setOnClickListener {
            showSortDialog()
        }

        fAdapter = FavoritesFavsAdapter(arrayListOf())
        transactionsFragBinding.favoritesRvFavs.adapter = fAdapter
        passInitialData()

        layoutManager = LinearLayoutManager(activity)
        transactionsFragBinding.favoritesRvFavs.layoutManager = layoutManager
        transactionsFragBinding.favoritesRvFavs.addItemDecoration(MarginItemDecoration(resources.displayMetrics, VERTICAL_SPACE))

        addRVScrollListener()

        return transactionsFragBinding.root
    }

    override fun onResume() {
        super.onResume()
//        fAdapter.notifyDataSetChanged()
    }

    private fun passInitialData() {
        dataStartingIndex = 0
        CoroutineScope(Dispatchers.Main).launch {
            isLoading = true
            transactionsFragBinding.favoritesInitialProgressBar.visibility = View.VISIBLE
            val firestoreHandler = FirestoreHandler.getInstance(transactionsFragBinding.root.context)
            val googleBooksAPIHandler = GoogleBooksAPIHandler()

            val returnedObjList = firestoreHandler.getAllFavorites()
            if(returnedObjList != null) {
                favoritesObjList = returnedObjList
                val endIndex = (dataStartingIndex + displayIncrement).coerceAtMost(favoritesObjList.size)
                Log.d("FavoritesFragment", "End index at initial data is $endIndex")
                val favoritesBookList = ArrayList(favoritesObjList.subList(dataStartingIndex, endIndex))
                dataStartingIndex = endIndex

                if(dataStartingIndex == favoritesObjList.size) {
                    hasMoreData = false
                }

                fAdapter.addBooks(favoritesBookList)
            }
            transactionsFragBinding.favoritesInitialProgressBar.visibility = View.GONE
            isLoading = false
        }
    }

    private fun addRVScrollListener() {
        transactionsFragBinding.favoritesRvFavs.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                if(!isLoading && hasMoreData) {
                    if(visibleItemCount + firstVisibleItemPosition >= totalItemCount
                        && firstVisibleItemPosition >= 0
                        && totalItemCount >= 10) {
                        incrementData()
                    }
                }
            }
        })
    }

    private fun incrementData() {
        isLoading = true
        transactionsFragBinding.favoritesScrollProgressBar.visibility = View.VISIBLE
        val layoutParams = transactionsFragBinding.favoritesRvFavs.layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.bottomMargin = 40
        val googleBooksAPIHandler = GoogleBooksAPIHandler()

        CoroutineScope(Dispatchers.Main).launch {
            val endIndex = (dataStartingIndex + displayIncrement).coerceAtMost(favoritesObjList.size)
            val newDataObjs = ArrayList(favoritesObjList.subList(dataStartingIndex, endIndex))
            Log.d("FavoritesFragment", "Incrementing data with these new book IDs: $newDataObjs")
            dataStartingIndex = endIndex

            if(dataStartingIndex == favoritesObjList.size) {
                hasMoreData = false
            }

            fAdapter.addBooks(newDataObjs)
            transactionsFragBinding.favoritesScrollProgressBar.visibility = View.GONE
            layoutParams.bottomMargin = 0
            isLoading = false
        }
    }

    private fun initPreferences() {
        val sp = PreferenceManager.getDefaultSharedPreferences(activity)

        val strPrefSortOption = sp.getString(FAVORITES_SORT_PREF, "")
        val strPrefFilterOption = sp.getString(FAVORITES_FILTER_PREF, "")

        val prefSortOption = SortOption.entries.firstOrNull { it.name == strPrefSortOption }
        val prefFilterOption = FilterOption.entries.firstOrNull { it.name == strPrefFilterOption }

        prefSortOption?.let {
            activeSortOption = it
        }

        prefFilterOption?.let {
            activeFilterOption = it
        }
    }

    private fun showSortDialog() {
        val bottomSheetDialog = BottomSheetDialog(requireActivity(), R.style.BottomSheetDialog)
        sortDialogBinding = ComponentDialogFavsBinding.inflate(layoutInflater)
        bottomSheetDialog.setContentView(sortDialogBinding!!.root)

        bottomSheetDialog.setOnDismissListener {
            sortDialogBinding = null

            sortDialogOptionButtons = null
            tempActiveSortOption = null

            filterButtons = null
            tempActiveFilterOption = null
        }

        sortDialogBinding!!.dialogFavsBtnConfirmbtn.setOnClickListener {
            // save selected sorting option after user hits confirm button
            tempActiveSortOption?.let {
                activeSortOption = it

                val sp = PreferenceManager.getDefaultSharedPreferences(activity)
                val editor = sp.edit()
                editor.putString(FAVORITES_SORT_PREF, it.name)
                editor.apply()
            }

            // save selected filter option after user hits confirm button
            tempActiveFilterOption?.let {
                activeFilterOption = it

                val sp = PreferenceManager.getDefaultSharedPreferences(activity)
                val editor = sp.edit()
                editor.putString(FAVORITES_FILTER_PREF, it.name)
                editor.apply()
            }

            bottomSheetDialog.dismiss()
        }

        // initialize sorting option buttons and highlight the active one
        initSortOptionButtons()

        // initialize search filter buttons and highlight the active one
        initFilterButtons()

        bottomSheetDialog.show()
    }

    private fun initSortOptionButtons() {
        sortDialogOptionButtons = sortDialogBinding?.let {
            listOf(
                SortOption.NEWEST to it.dialogFavsBtnSortNewest,
                SortOption.TITLE to it.dialogFavsBtnSortTitle,
                SortOption.AUTHOR to it.dialogFavsBtnSortAuthorname,
            )
        }

        sortDialogOptionButtons!!.forEach { (option, button) ->
            button.setOnClickListener {
                // highlight the selected sort option but don't save it yet (user must confirm)
                tempActiveSortOption = option
                highlightSortOption()
            }
        }

        highlightSortOption()
    }

    private fun initFilterButtons() {
        filterButtons = sortDialogBinding?.let {
            listOf(
                FilterOption.ALL to it.dialogFavsBtnFilterAll,
                FilterOption.TO_PICKUP to it.dialogFavsBtnFilterTopickup,
                FilterOption.TO_RETURN to it.dialogFavsBtnFilterToreturn,
                FilterOption.OVERDUE to it.dialogFavsBtnFilterOverdue,
                FilterOption.RETURNED to it.dialogFavsBtnFilterReturned,
                FilterOption.CANCELLED to it.dialogFavsBtnFilterCancelled,
            )
        }

        filterButtons!!.forEach { (option, button) ->
            button.setOnClickListener {
                tempActiveFilterOption = option
                highlightFilterButton()
            }
        }

        highlightFilterButton()
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

    private fun highlightFilterButton() {
        filterButtons?.forEach { (option, button) ->
            activeFilterOption.let {
                // highlight newly selected filter option, otherwise highlight default/last chosen filter option
                // unhighlight the rest of the filter options
                val isHighlighted = option == tempActiveFilterOption || (tempActiveFilterOption == null && option == activeFilterOption)

                button.backgroundTintList = if (isHighlighted) ColorStateList.valueOf(ContextCompat.getColor(requireActivity(), R.color.main_green)) else null
                button.setTextColor(if (isHighlighted) Color.parseColor("#FFFFFF") else ContextCompat.getColor(requireActivity(), R.color.dark_background))
            }
        }
    }
}