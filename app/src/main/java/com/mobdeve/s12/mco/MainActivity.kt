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

        setFonts()
        addListenerLoginBtn()
        addListenerRegisterBtn()

    }

    fun setFonts() {
        viewBinding.mainTvSlogan.typeface = ResourcesCompat.getFont(this, R.font.sf_ui_text_regular)
        viewBinding.mainBtnLogin.typeface = ResourcesCompat.getFont(this, R.font.sf_ui_text_regular)
        viewBinding.mainBtnRegister.typeface = ResourcesCompat.getFont(this, R.font.sf_ui_text_regular)

    }

    fun addListenerLoginBtn() {
        viewBinding.mainBtnLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    fun addListenerRegisterBtn() {
        viewBinding.mainBtnRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}