package com.mobdeve.s12.mco

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.mobdeve.s12.mco.databinding.ActivityMainBinding

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}