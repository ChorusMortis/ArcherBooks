package com.mobdeve.s12.mco

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.mobdeve.s12.mco.databinding.ActivityRegisterBinding


class RegisterActivity : AppCompatActivity() {

    private lateinit var viewBinding : ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        viewBinding = ActivityRegisterBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        addListenerLoginHyperLink()
        addListenerCreateAccountBtn()
    }

    fun addListenerLoginHyperLink() {
        viewBinding.registerTvLoginhyperlink.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    fun addListenerCreateAccountBtn() {
        // TODO MCO3: Get input, validate if email address is unique, and add account to database
        // TODO MCO3: Add password and confirm password validation
        // TODO MCO3: Add input validation (1) empty fields, (2) whitespaces, (3) invalid characters
        // TODO MCO3: Password validation (1) length, (2) characters

        viewBinding.registerBtnRegisterbtn.setOnClickListener(View.OnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        })
    }
}