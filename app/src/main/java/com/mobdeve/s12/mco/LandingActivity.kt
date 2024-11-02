package com.mobdeve.s12.mco

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mobdeve.s12.mco.databinding.ActivityLandingBinding

class LandingActivity : AppCompatActivity() {

    private lateinit var viewBinding : ActivityLandingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        viewBinding = ActivityLandingBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        addListenerLoginBtn()
        addListenerRegisterBtn()

    }

    private fun addListenerLoginBtn() {
        viewBinding.mainBtnLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
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