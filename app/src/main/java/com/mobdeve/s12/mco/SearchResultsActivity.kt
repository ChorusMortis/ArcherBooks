package com.mobdeve.s12.mco

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.mobdeve.s12.mco.databinding.ActivitySearchResultsBinding

class SearchResultsActivity : AppCompatActivity() {
    companion object {
        private const val VERTICAL_SPACE = 25
    }

    private lateinit var viewBinding : ActivitySearchResultsBinding
    private lateinit var searchFilterButtons : List<Button>
    private var activeSearchFilterBtn : Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivitySearchResultsBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        initSearchFilterButtons()

        viewBinding.searchresultsRvResults.adapter = SearchResultsResultsAdapter(BookGenerator.generateSampleBooks())
        viewBinding.searchresultsRvResults.layoutManager = LinearLayoutManager(this)
        viewBinding.searchresultsRvResults.addItemDecoration(MarginItemDecoration(VERTICAL_SPACE))
    }

    private fun initSearchFilterButtons() {
        searchFilterButtons = listOf<Button>(
            viewBinding.searchresultsBtnFiltermatchall,
            viewBinding.searchresultsBtnFiltermatchtitle,
            viewBinding.searchresultsBtnFiltermatchauthor,
            viewBinding.searchresultsBtnFiltermatchsubject,
            viewBinding.searchresultsBtnFiltermatchpublisher
        )

        activeSearchFilterBtn = viewBinding.searchresultsBtnFiltermatchall
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
}