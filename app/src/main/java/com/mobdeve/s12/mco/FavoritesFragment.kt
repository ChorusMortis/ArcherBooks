package com.mobdeve.s12.mco

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.mobdeve.s12.mco.databinding.ComponentMyfavsSortDialogBinding
import com.mobdeve.s12.mco.databinding.FragmentFavoritesBinding

class FavoritesFragment : Fragment() {
    companion object {
        private const val VERTICAL_SPACE = 12
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

    private var sortDialogBinding : ComponentMyfavsSortDialogBinding? = null

    private var sortDialogOptionButtons : List<Pair<SortOption, Button>>? = null
    private var activeSortOption : SortOption = SortOption.NEWEST
    private var tempActiveSortOption : SortOption? = null

    private var filterButtons : List<Pair<FilterOption, Button>>? = null
    private var activeFilterOption : FilterOption = FilterOption.ALL
    private var tempActiveFilterOption : FilterOption? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        transactionsFragBinding = FragmentFavoritesBinding.inflate(inflater, container, false)

        transactionsFragBinding.favoritesBtnSort.setOnClickListener {
            showSortDialog()
        }

        transactionsFragBinding.favoritesRvFavs.adapter = FavoritesFavsAdapter(BookGenerator.generateSampleBooks())
        transactionsFragBinding.favoritesRvFavs.layoutManager = LinearLayoutManager(activity)
        transactionsFragBinding.favoritesRvFavs.addItemDecoration(MarginItemDecoration(resources.displayMetrics, VERTICAL_SPACE))

        return transactionsFragBinding.root
    }

    private fun showSortDialog() {
        val bottomSheetDialog = BottomSheetDialog(requireActivity(), R.style.BottomSheetDialog)
        sortDialogBinding = ComponentMyfavsSortDialogBinding.inflate(layoutInflater)
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
            }

            // save selected filter option after user hits confirm button
            tempActiveFilterOption?.let {
                activeFilterOption = it
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