package com.mobdeve.s12.mco

import android.content.Intent
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
    private lateinit var mbbRecyclerView: RecyclerView
    private lateinit var mbbAdapter: HomeMBBAdapter
    private lateinit var favRecyclerView: RecyclerView
    private lateinit var favAdapter: HomeFavAdapter

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
        setFavRecyclerView()
    }

    private fun setImageResources() {
        viewBinding.homeIvLogo.setImageResource(R.drawable.archerbooks_logo_green)
        viewBinding.homeIvSearchIcon.setImageResource(R.drawable.search_icon_white)
    }

    private fun addListenerSearchBtn() {
        viewBinding.homeBtnSearch.setOnClickListener {
            val intent = Intent(this, SearchResultsFragment::class.java)
            startActivity(intent)
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

    private fun setFavRecyclerView() {
        val sampleFavBooks = books.shuffled().take(1)

        this.favRecyclerView = viewBinding.homeRvFav
        this.favAdapter = HomeFavAdapter(ArrayList(sampleFavBooks))
        this.favRecyclerView.adapter = this.favAdapter
        val favLinearLayoutManager = LinearLayoutManager(this)
        favLinearLayoutManager.orientation = LinearLayoutManager.HORIZONTAL
        this.favRecyclerView.layoutManager = favLinearLayoutManager
    }
}