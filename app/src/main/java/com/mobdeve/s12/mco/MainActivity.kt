package com.mobdeve.s12.mco

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mobdeve.s12.mco.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    companion object {
        const val NEXUS_5X_HEIGHT = 1920f
        const val MEDIUM_PHONE_HEIGHT = 2400f
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        viewBinding.mainBtnLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        viewBinding.mainBtnRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}