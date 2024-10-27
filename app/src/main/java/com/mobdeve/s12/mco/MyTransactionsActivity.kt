package com.mobdeve.s12.mco

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mobdeve.s12.mco.databinding.ActivityMyTransactionsBinding

class MyTransactionsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewBinding = ActivityMyTransactionsBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
    }
}