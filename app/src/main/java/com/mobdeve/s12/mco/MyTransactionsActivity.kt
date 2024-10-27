package com.mobdeve.s12.mco

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.mobdeve.s12.mco.databinding.ActivityMyTransactionsBinding

class MyTransactionsActivity : AppCompatActivity() {
    companion object {
        private const val VERTICAL_SPACE = 24
    }

    private lateinit var myTransactionsBinding : ActivityMyTransactionsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        myTransactionsBinding = ActivityMyTransactionsBinding.inflate(layoutInflater)
        setContentView(myTransactionsBinding.root)

        myTransactionsBinding.mytransRvTransactions.adapter = MyTransactionsTransAdapter(BookGenerator.generateSampleBooks())
        myTransactionsBinding.mytransRvTransactions.layoutManager = LinearLayoutManager(this)
        myTransactionsBinding.mytransRvTransactions.addItemDecoration(MarginItemDecoration(resources.displayMetrics, VERTICAL_SPACE))
    }
}