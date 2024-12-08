package com.mobdeve.s12.mco

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mobdeve.s12.mco.databinding.FragmentHomeBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private val books = BookGenerator.generateSampleBooks()
    private val rvBooks = ArrayList<BookModel>()
    private val users = UserGenerator.generateSampleUsers()
    private val transactions = TransactionGenerator.generateSampleTransactions(books, users)

    private lateinit var viewBinding : FragmentHomeBinding
    private lateinit var rvRecyclerView: RecyclerView
    private lateinit var rvAdapter: HomeRVAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        viewBinding = FragmentHomeBinding.inflate(inflater, container, false)

        setDisplayName()
        setRVRecyclerView()
        addListenerSearchBtn()
        scrollToTop()
        requestNotifPerms()

        val botd = books.random() // TODO MCO3: Allow it to randomly query a book from the API
        setContentBOTD(botd)
        addListenerBOTD(botd)
        return viewBinding.root
    }

    override fun onResume() {
        super.onResume()

        updateRecentlyViewedRV()
    }

    private fun requestNotifPerms() {
        if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {

            // only request notification permissions for Android 13+ (SDK 33 and above)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ActivityCompat.requestPermissions(requireActivity(),
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001)
            }
        }
    }

    private fun setDisplayName() {
        val authHandler = AuthHandler.getInstance(requireActivity())
        val authFullName = authHandler.getUserFullName()
        // get full name from Firebase Auth (only works if user has logged into Google before)
        authFullName?.let {
            viewBinding.homeTvUserName.text = authFullName
            return
        }

        // otherwise, get full name from database (what user entered in registration form)
        CoroutineScope(Dispatchers.Main).launch {
            val firestoreHandler = FirestoreHandler.getInstance(requireActivity())
            val userModel = firestoreHandler.getCurrentUserModel() ?: return@launch
            val userDbFullName = "${userModel.firstName} ${userModel.lastName}"
            viewBinding.homeTvUserName.text = userDbFullName
        }
    }

    private fun setRVRecyclerView() {
        rvRecyclerView = viewBinding.homeRvVr
        // data is initially empty, get actual data later from db
        rvAdapter = HomeRVAdapter(rvBooks)
        rvRecyclerView.adapter = rvAdapter
        rvRecyclerView.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
    }

    private fun updateRecentlyViewedRV() {
        CoroutineScope(Dispatchers.Main).launch {
            // get recently viewed books from user via db
            Log.d("updateRecentlyViewedRV", "Fetching user's recently viewed books array")
            val firestoreHandler = FirestoreHandler.getInstance(requireActivity())
            val rvBooksFromDb = firestoreHandler.getRecentlyViewedBooks()
            if (rvBooksFromDb == null) {
                Log.w("updateRecentlyViewedRV", "User has no recently viewed books array")
                setNoRecentBooksMessageVisibility(emptyList())
                return@launch
            }

            rvBooks.clear()
            rvBooks.addAll(rvBooksFromDb)
            rvAdapter.notifyItemRangeChanged(0, rvBooks.size)
            setNoRecentBooksMessageVisibility(rvBooksFromDb)
        }
    }

    private fun addListenerSearchBtn() {
        viewBinding.homeBtnSearchBtn.setOnClickListener(View.OnClickListener {
            val navHostFragment = requireActivity().supportFragmentManager.findFragmentById(R.id.navbar_fragment_controller) as NavHostFragment
            val navController = navHostFragment.navController
            navController.navigate(R.id.action_search)
        })
    }

    private fun scrollToTop() {
        viewBinding.homeSlContainer.post{
            viewBinding.homeSlContainer.scrollTo(0, 0)
        }
    }

    private fun setContentBOTD(botd: BookModel) {
//        viewBinding.homeBotdIvBg.setImageResource(botd.coverResource)
//        viewBinding.homeBotdIvCover.setImageResource(botd.coverResource)
        Glide.with(this)
            .load(botd.coverResource)
            .into(viewBinding.homeBotdIvBg)
        Glide.with(this)
            .load(botd.coverResource)
            .into(viewBinding.homeBotdIvCover)
        viewBinding.homeBotdTvTitle.text = botd.title
        viewBinding.homeBotdTvAuthors.text = botd.authors.joinToString(", ")
        viewBinding.homeBotdTvDate.text = botd.publishYear
    }

    private fun addListenerBOTD(botd: BookModel) {
        viewBinding.homeBotdClContainer.setOnClickListener {
            val intent = Intent(activity, BookDetailsActivity::class.java)
            intent.putExtra(BookDetailsActivity.ID_KEY, botd.id)
            intent.putExtra(BookDetailsActivity.TITLE_KEY, botd.title)
            intent.putExtra(BookDetailsActivity.YEAR_PUBLISHED_KEY, botd.publishYear)
            intent.putExtra(BookDetailsActivity.AUTHORS_KEY, botd.authors.joinToString(", "))
            intent.putExtra(BookDetailsActivity.COVER_KEY, botd.coverResource)
            intent.putExtra(BookDetailsActivity.PUBLISHER_KEY, botd.publisher)
            intent.putExtra(BookDetailsActivity.STATUS_KEY, "Book Available") // TODO MCO3 comes from transaction
            intent.putExtra(BookDetailsActivity.SHELF_LOCATION_KEY, botd.shelfLocation)
            intent.putExtra(BookDetailsActivity.DESCRIPTION_KEY, botd.description)
            intent.putExtra(BookDetailsActivity.PAGES_KEY, botd.pageCount)
            startActivity(intent)
        }
    }

    private fun setNoRecentBooksMessageVisibility(rvBooks: List<BookModel>?) {
        val visibility = if (rvBooks.isNullOrEmpty()) View.VISIBLE else View.GONE
        viewBinding.homeTvNorecentMessage.visibility = visibility
    }
}