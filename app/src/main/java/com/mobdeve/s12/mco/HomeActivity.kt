package com.mobdeve.s12.mco

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mobdeve.s12.mco.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {

    private val books = BookGenerator.generateSampleBooks()

    private lateinit var viewBinding : ActivityHomeBinding
    private lateinit var forYouRecyclerView: RecyclerView
    private lateinit var forYouCardAdapter: HomeForYouAdapter
    private lateinit var mbbRecyclerView: RecyclerView
    private lateinit var mbbAdapter: HomeMBBAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        viewBinding = ActivityHomeBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        // add button listeners
        addListenerSearchBtn()
        clearFocus()

        // set logos and icons' image resources on the home screen
        setImageResources()

        // set recycler views
        setForYouRecyclerView()
        setMBBRecyclerView()
    }

    private fun setImageResources() {
        viewBinding.homeIvLogo.setImageResource(R.drawable.archerbooks_logo_green)
        viewBinding.homeIvSearchIcon.setImageResource(R.drawable.search_icon_white)
    }

    private fun addListenerSearchBtn() {
        viewBinding.homeBtnSearch.setOnClickListener {
            val intent = Intent(this, SearchResultsActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun clearFocus() {
        viewBinding.root.requestFocus()
    }

    private fun setForYouRecyclerView() {
        this.forYouRecyclerView = viewBinding.homeRvForyou
        this.forYouCardAdapter = HomeForYouAdapter(books)
        this.forYouRecyclerView.adapter = this.forYouCardAdapter
        val forYouLinearLayoutManager = LinearLayoutManager(this)
        forYouLinearLayoutManager.orientation = LinearLayoutManager.HORIZONTAL
        this.forYouRecyclerView.layoutManager = forYouLinearLayoutManager
    }

    private fun setMBBRecyclerView() {
        val sampleBorrowedBooks = books.shuffled().take(3)

        this.mbbRecyclerView = viewBinding.homeRvMbb
        this.mbbAdapter = HomeMBBAdapter(ArrayList(sampleBorrowedBooks))
        this.mbbRecyclerView.adapter = this.mbbAdapter
        val mbbLinearLayoutManager = LinearLayoutManager(this)
        mbbLinearLayoutManager.orientation = LinearLayoutManager.HORIZONTAL
        this.mbbRecyclerView.layoutManager = mbbLinearLayoutManager
    }
}