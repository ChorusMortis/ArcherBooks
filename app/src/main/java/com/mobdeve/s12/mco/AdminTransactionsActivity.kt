package com.mobdeve.s12.mco

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mobdeve.s12.mco.databinding.ActivityAdminTransactionsBinding
import com.mobdeve.s12.mco.databinding.FragmentHomeBinding

class AdminTransactionsActivity : AppCompatActivity() {

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
        this.rvRecyclerView = viewBinding.adminTransRv
        this.rvAdapter = AdminTransactionsAdapter(ArrayList(transactions))
        this.rvRecyclerView.adapter = this.rvAdapter
        val transLinearLayoutManager = LinearLayoutManager(this)
        transLinearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        this.rvRecyclerView.layoutManager = transLinearLayoutManager
    }

    private fun initLogoutButton() {
        viewBinding.adminTransBtnLogout.setOnClickListener {
            // end activity which Login started
            finish()
        }
    }
}