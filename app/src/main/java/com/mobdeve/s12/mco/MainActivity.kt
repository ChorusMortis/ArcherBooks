package com.mobdeve.s12.mco

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.mobdeve.s12.mco.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var viewBinding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        addListenerLoginBtn()
        addListenerRegisterBtn()

    }

    private fun addListenerLoginBtn() {
        viewBinding.mainBtnLogin.setOnClickListener {
            val intent = Intent(this, BookDetailsActivity::class.java)
            startActivity(intent)
        }
    }

    private fun addListenerRegisterBtn() {
        viewBinding.mainBtnRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}