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
import com.mobdeve.s12.mco.databinding.FragmentFavoritesBinding
import com.mobdeve.s12.mco.databinding.ComponentMyfavsSortDialogBinding

class FavoritesFragment : Fragment() {
    companion object {
        private const val VERTICAL_SPACE = 24
    }

    private enum class SortOption {
        TITLE,
        AUTHOR,
        NEWEST,
        OLDEST,
    }

    private lateinit var myFavoritesBinding : FragmentFavoritesBinding
    private lateinit var filterButtons : List<Button>
    private var activeFilterBtn : Button? = null

    private var sortDialogBinding : ComponentMyfavsSortDialogBinding? = null
    private var sortDialogOptionButtons : List<Pair<SortOption, Button>>? = null
    private var activeSortOption : SortOption = SortOption.TITLE
    private var tempSortOption : SortOption? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
//        super.onCreate(savedInstanceState)
//        myFavoritesBinding = FragmentFavoritesBinding.inflate(layoutInflater)
//        setContentView(myFavoritesBinding.root)
        myFavoritesBinding = FragmentFavoritesBinding.inflate(inflater, container, false)


        initFilterButtons()

        myFavoritesBinding.myfavsIbSortbtn.setOnClickListener {
            showSortDialog()
        }

        myFavoritesBinding.myfavsRvFavorites.adapter = MyFavoritesFavsAdapter(BookGenerator.generateSampleBooks())
        myFavoritesBinding.myfavsRvFavorites.layoutManager = LinearLayoutManager(activity)
        myFavoritesBinding.myfavsRvFavorites.addItemDecoration(MarginItemDecoration(resources.displayMetrics, VERTICAL_SPACE))

        return myFavoritesBinding.root
    }

    private fun initFilterButtons() {
        filterButtons = listOf(
            myFavoritesBinding.myfavsBtnFiltermatchall,
            myFavoritesBinding.myfavsBtnFiltermatchpending,
            myFavoritesBinding.myfavsBtnFiltermatchpickedup,
            myFavoritesBinding.myfavsBtnFiltermatchreturned,
            myFavoritesBinding.myfavsBtnFiltermatchcancelled,
            myFavoritesBinding.myfavsBtnFiltermatchpickupmissed,
            myFavoritesBinding.myfavsBtnFiltermatchreturnmissed
        )

        activeFilterBtn = myFavoritesBinding.myfavsBtnFiltermatchall
        filterButtons.forEach { button ->
            button.setOnClickListener {
                activeFilterBtn?.let {
                    it.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireActivity(), R.color.search_filter_button))
                    button.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireActivity(), R.color.main_green))
                    activeFilterBtn = button
                }
            }
        }
    }

    private fun showSortDialog() {
        val bottomSheetDialog = BottomSheetDialog(requireActivity())
        sortDialogBinding = ComponentMyfavsSortDialogBinding.inflate(layoutInflater)
        bottomSheetDialog.setContentView(sortDialogBinding!!.root)

        sortDialogOptionButtons = listOf(
            SortOption.TITLE to sortDialogBinding!!.dialogMyfavsBtnFiltertitle,
            SortOption.AUTHOR to sortDialogBinding!!.dialogMyfavsBtnFilterauthor,
            SortOption.NEWEST to sortDialogBinding!!.dialogMyfavsBtnFilternewest,
            SortOption.OLDEST to sortDialogBinding!!.dialogMyfavsBtnFilteroldest
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

        sortDialogBinding!!.dialogMyfavsBtnConfirmbtn.setOnClickListener {
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