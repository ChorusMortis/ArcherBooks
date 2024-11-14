package com.mobdeve.s12.mco

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mobdeve.s12.mco.databinding.ActivityAdminTransactionsBinding

class AdminTransactionsActivity : AppCompatActivity() {
    companion object {
        private const val VERTICAL_SPACE = 12
    }

    private val books = BookGenerator.generateSampleBooks()
    private val users = UserGenerator.generateSampleUsers()
    private val transactions = TransactionGenerator.generateSampleTransactions(books, users)

    private lateinit var viewBinding : ActivityAdminTransactionsBinding
    private lateinit var rvRecyclerView: RecyclerView
    private lateinit var rvAdapter: AdminTransactionsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityAdminTransactionsBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        setRVRecyclerView()
        initLogoutButton()
    }

    private fun setRVRecyclerView() {
        rvRecyclerView = viewBinding.adminTransRv
        rvAdapter = AdminTransactionsAdapter(ArrayList(transactions))
        rvRecyclerView.adapter = rvAdapter
        rvRecyclerView.layoutManager = LinearLayoutManager(this)
        rvRecyclerView.addItemDecoration(MarginItemDecoration(resources.displayMetrics, VERTICAL_SPACE))
    }

    private fun initLogoutButton() {
        viewBinding.adminTransBtnLogout.setOnClickListener {
            // end activity which Login started
            finish()
        }
    }
}