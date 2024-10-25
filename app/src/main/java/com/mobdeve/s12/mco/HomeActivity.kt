package com.mobdeve.s12.mco

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mobdeve.s12.mco.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {

    private lateinit var viewBinding : ActivityHomeBinding
    private val books = BookGenerator.generateSampleBooks()

    override fun onCreate(savedInstanceState: Bundle?) {
        viewBinding = ActivityHomeBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        viewBinding.homeIvLogo.setImageResource(R.drawable.archerbooks_logo_green)
        viewBinding.homeIvSearchIcon.setImageResource(R.drawable.search_solid)
    }
}