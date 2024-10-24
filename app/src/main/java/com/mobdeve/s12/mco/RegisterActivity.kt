package com.mobdeve.s12.mco

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.mobdeve.s12.mco.databinding.ActivityRegisterBinding


class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewBinding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        // go to login activity when user taps on "Log in" text
        viewBinding.registerTvLoginhyperlink.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        // dynamic sizing of the content (white background)
        val heightPixels = resources.displayMetrics.heightPixels.toFloat()
        val params = viewBinding.registerClContent.layoutParams as ConstraintLayout.LayoutParams
        if(heightPixels <= MainActivity.NEXUS_5X_HEIGHT) {
            params.matchConstraintPercentHeight = 0.95f
        } else {
            params.matchConstraintPercentHeight = 0.85f
        }
    }
}