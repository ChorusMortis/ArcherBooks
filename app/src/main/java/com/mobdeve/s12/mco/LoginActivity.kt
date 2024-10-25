package com.mobdeve.s12.mco

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.mobdeve.s12.mco.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var viewBinding : ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        viewBinding = ActivityLoginBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        addListenerRegisterHyperLink()
        addListenerSignInBtn()
        setFonts()

    }

    fun setFonts() {
        viewBinding.loginTvLogintitle.typeface = ResourcesCompat.getFont(this, R.font.sf_ui_text_regular)
        viewBinding.loginTvHeadertitle.typeface = ResourcesCompat.getFont(this, R.font.sf_ui_text_bold)
        viewBinding.loginTvHeadersubtitle.typeface = ResourcesCompat.getFont(this, R.font.sf_ui_text_regular)
        viewBinding.loginEtEmail.typeface = ResourcesCompat.getFont(this, R.font.sf_ui_text_regular)
        viewBinding.loginEtPassword.typeface = ResourcesCompat.getFont(this, R.font.sf_ui_text_regular)
        viewBinding.loginTvForgotpassword.typeface = ResourcesCompat.getFont(this, R.font.sf_ui_text_bold)
        viewBinding.loginBtnLoginbtn.typeface = ResourcesCompat.getFont(this, R.font.sf_ui_text_medium)
        viewBinding.loginTvAltsignintext.typeface = ResourcesCompat.getFont(this, R.font.sf_ui_text_regular)
        viewBinding.loginBtnSigningoogle.typeface = ResourcesCompat.getFont(this, R.font.sf_ui_text_regular)
        viewBinding.loginTvRegistertitle.typeface = ResourcesCompat.getFont(this, R.font.sf_ui_text_regular)
        viewBinding.loginTvRegisterhyperlink.typeface = ResourcesCompat.getFont(this, R.font.sf_ui_text_bold)
    }

    fun addListenerRegisterHyperLink() {
        // go to register activity when user taps on "Sign up" text
        viewBinding.loginTvRegisterhyperlink.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    fun addListenerSignInBtn() {
        // TODO MCO3: Get input, validate if email address is unique, and add account to database
        // TODO MCO3: Add password and confirm password validation
        // TODO MCO3: Add input validation (1) empty fields, (2) whitespaces, (3) invalid characters
        // TODO MCO3: Password validation (1) length, (2) characters

        viewBinding.loginBtnLoginbtn.setOnClickListener(View.OnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        })
    }
}