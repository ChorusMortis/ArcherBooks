package com.mobdeve.s12.mco

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.mobdeve.s12.mco.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var viewBinding : ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        viewBinding = ActivityLoginBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        dynamicallySizeContent()
        addListenerRegisterHyperLink()
        addListenerSignInBtn()
    }

    fun dynamicallySizeContent() {
        // dynamic sizing of the content (white background)
        val heightPixels = resources.displayMetrics.heightPixels.toFloat()
        val params = viewBinding.loginClContent.layoutParams as ConstraintLayout.LayoutParams
        if(heightPixels <= MainActivity.NEXUS_5X_HEIGHT) {
            params.matchConstraintPercentHeight = 0.95f
        } else {
            params.matchConstraintPercentHeight = 0.85f
        }
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