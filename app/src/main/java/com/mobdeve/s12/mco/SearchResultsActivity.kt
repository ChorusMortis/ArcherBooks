package com.mobdeve.s12.mco

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.mobdeve.s12.mco.databinding.ActivitySearchResultsBinding

class SearchResultsActivity : AppCompatActivity() {
    companion object {
        private const val VERTICAL_SPACE = 25
    }

    private lateinit var viewBinding : ActivitySearchResultsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivitySearchResultsBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        viewBinding.searchresultsRvResults.adapter = SearchResultsResultsAdapter(BookGenerator.generateSampleBooks())
        viewBinding.searchresultsRvResults.layoutManager = LinearLayoutManager(this)
        viewBinding.searchresultsRvResults.addItemDecoration(MarginItemDecoration(VERTICAL_SPACE))
    }
}