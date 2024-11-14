package com.mobdeve.s12.mco

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.mobdeve.s12.mco.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var viewBinding : ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        viewBinding = ActivityLoginBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        addListenerRegisterHyperLink()
        addListenerSignInBtn()
        addListenerSignInWithGoogleBtn()
    }

    private fun addListenerRegisterHyperLink() {
        viewBinding.loginTvRegisterhyperlink.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun addListenerSignInBtn() {
        // TODO MCO3: Get input, validate if email address is unique, and add account to database
        // TODO MCO3: Add password and confirm password validation
        // TODO MCO3: Add input validation (1) empty fields, (2) whitespaces, (3) invalid characters
        // TODO MCO3: Password validation (1) length, (2) characters

        viewBinding.loginBtnLoginbtn.setOnClickListener(View.OnClickListener {
            val intent : Intent
            intent = if(viewBinding.loginEtEmail.text.toString() == "Admin") { // TODO MCO3: Change this to detect user auth with Firebase
                Intent(this, AdminTransactionsActivity::class.java)
            } else {
                Intent(this, MainActivity::class.java)
            }
            startActivity(intent)
        })
    }

    private fun addListenerSignInWithGoogleBtn() {
        // TODO MCO3: Add Google Handling
        viewBinding.loginBtnSigningoogle.setOnClickListener(View.OnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        })
    }
}