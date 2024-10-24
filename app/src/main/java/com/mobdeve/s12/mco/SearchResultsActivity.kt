package com.mobdeve.s12.mco

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mobdeve.s12.mco.databinding.ActivitySearchResultsBinding

class SearchResultsActivity : AppCompatActivity() {

    private lateinit var viewBinding : ActivitySearchResultsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivitySearchResultsBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)


    }
}