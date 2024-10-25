package com.mobdeve.s12.mco

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mobdeve.s12.mco.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {

    private val books = BookGenerator.generateSampleBooks()

    private lateinit var viewBinding : ActivityHomeBinding
    private lateinit var forYouRecyclerView: RecyclerView
    private lateinit var forYouCardAdapter: HomeForYouAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        viewBinding = ActivityHomeBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        // set logos and icons' image resources on the home screen
        viewBinding.homeIvLogo.setImageResource(R.drawable.archerbooks_logo_white)
        viewBinding.homeIvSearchIcon.setImageResource(R.drawable.search_icon_green)

        // set the recycler view and adapter
        this.forYouRecyclerView = viewBinding.homeRvForyou
        this.forYouCardAdapter = HomeForYouAdapter(books)
        this.forYouRecyclerView.adapter = this.forYouCardAdapter
        val forYouLinearLayoutManager = LinearLayoutManager(this)
        forYouLinearLayoutManager.orientation = LinearLayoutManager.HORIZONTAL
        this.forYouRecyclerView.layoutManager = forYouLinearLayoutManager
    }
}