package com.mobdeve.s12.mco

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mobdeve.s12.mco.databinding.ActivityHome2Binding

class FragmentHome : Fragment() {

    private val books = BookGenerator.generateSampleBooks()

    private lateinit var viewBinding : ActivityHome2Binding
    private lateinit var rvRecyclerView: RecyclerView
    private lateinit var rvAdapter: HomeRVAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
//        super.onCreate(savedInstanceState)
//        viewBinding = ActivityHome2Binding.inflate(layoutInflater)
//        setContentView(viewBinding.root)
//
//        setRVRecyclerView()
//        addListenerSearchBtn()
////        addListenerNavBar()
//
//        val botd = books.random() // TODO MCO3: Allow it to randomly query a book from the API
//        setContentBOTD(botd)
//        addListenerBOTD(botd)

        viewBinding = ActivityHome2Binding.inflate(inflater, container, false)

        setRVRecyclerView()
        addListenerSearchBtn()

        val botd = books.random() // TODO MCO3: Allow it to randomly query a book from the API
        setContentBOTD(botd)
        addListenerBOTD(botd)
        return viewBinding.root
    }

    private fun setRVRecyclerView() {
        this.rvRecyclerView = viewBinding.homeRvVr
        this.rvAdapter = HomeRVAdapter(ArrayList(books))
        this.rvRecyclerView.adapter = this.rvAdapter
        val mbbLinearLayoutManager = LinearLayoutManager(activity)
        mbbLinearLayoutManager.orientation = LinearLayoutManager.HORIZONTAL
        this.rvRecyclerView.layoutManager = mbbLinearLayoutManager
    }

    private fun addListenerSearchBtn() {
        viewBinding.homeBtnSearchBtn.setOnClickListener(View.OnClickListener {
//            val intent = Intent(activity, SearchResultsActivity::class.java)
//            startActivity(intent)
            val navHostFragment = requireActivity().supportFragmentManager.findFragmentById(R.id.navbar_fragment_controller) as NavHostFragment
            val navController = navHostFragment.navController
            navController.navigate(R.id.action_search)
        })
    }

    private fun setContentBOTD(botd: BookModel) {
        viewBinding.homeBotdIvBg.setImageResource(botd.coverResource)
        viewBinding.homeBotdIvCover.setImageResource(botd.coverResource)
        viewBinding.homeBotdTvTitle.text = botd.title
        viewBinding.homeBotdTvAuthors.text = botd.authors.joinToString(", ")
        viewBinding.homeBotdTvDate.text = botd.publishYear.toString()
    }

    private fun addListenerBOTD(botd: BookModel) {
        viewBinding.homeBotdClContainer.setOnClickListener(View.OnClickListener {
            val intent = Intent(activity, BookDetailsActivity::class.java)
            intent.putExtra(BookDetailsActivity.TITLE_KEY, botd.title)
            intent.putExtra(BookDetailsActivity.YEAR_PUBLISHED_KEY, botd.publishYear)
            intent.putExtra(BookDetailsActivity.AUTHORS_KEY, botd.authors.joinToString(", "))
            intent.putExtra(BookDetailsActivity.COVER_KEY, botd.coverResource)
            intent.putExtra(BookDetailsActivity.STATUS_KEY, "Book Available") // TODO MCO3 comes from transaction
            intent.putExtra(BookDetailsActivity.SHELF_LOCATION_KEY, botd.shelfLocation)
            intent.putExtra(BookDetailsActivity.DESCRIPTION_KEY, botd.description)
            intent.putExtra(BookDetailsActivity.PAGES_KEY, botd.pageCount)
            startActivity(intent)
        })
    }

//    private fun addListenerNavBar() {
//        viewBinding.homeNavbar.navbarBtnTransactions.setOnClickListener(View.OnClickListener {
//            val intent = Intent(this, MyTransactionsActivity::class.java)
//            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
//            startActivity(intent)
//            viewBinding.homeNavbar.navbarIvTransactionsIcon.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.main_green))
//        })
//
//        viewBinding.homeNavbar.navbarBtnHome.setOnClickListener(View.OnClickListener {
//            val intent = Intent(this, Home2Activity::class.java)
//            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
//            startActivity(intent)
//        })
//    }

}