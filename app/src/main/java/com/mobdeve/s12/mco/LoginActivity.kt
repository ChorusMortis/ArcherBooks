package com.mobdeve.s12.mco

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.mobdeve.s12.mco.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewBinding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        // go to register activity when user taps on "Sign up" text
        viewBinding.loginTvRegisterhyperlink.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
            finish()
        }

        // dynamic sizing of the content (white background)
        val heightPixels = resources.displayMetrics.heightPixels.toFloat()
        val params = viewBinding.loginClContent.layoutParams as ConstraintLayout.LayoutParams
        if(heightPixels <= MainActivity.NEXUS_5X_HEIGHT) {
            params.matchConstraintPercentHeight = 0.95f
        } else {
            params.matchConstraintPercentHeight = 0.85f
        }
    }
}