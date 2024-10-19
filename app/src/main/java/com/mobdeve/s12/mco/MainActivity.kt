package com.mobdeve.s12.mco

import android.os.Bundle
import androidx.activity.ComponentActivity
import com.mobdeve.s12.mco.databinding.ActivityMainBinding

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
    }
}