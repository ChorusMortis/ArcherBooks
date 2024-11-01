package com.mobdeve.s12.mco

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mobdeve.s12.mco.databinding.ActivityBookDetailsBinding
import android.graphics.RenderEffect
import android.graphics.Shader
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.os.Build
import android.view.View
import android.widget.ImageView
import androidx.annotation.RequiresApi


class BookDetailsActivity : AppCompatActivity() {

    private lateinit var viewBinding : ActivityBookDetailsBinding

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityBookDetailsBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

//        applyBlurToView(viewBinding.bookDetailsIvBgCover, 80f)
    }

    @RequiresApi(Build.VERSION_CODES.S)
    fun applyBlurToView(view: View, blurRadius: Float) {
        view.setRenderEffect(
            RenderEffect.createBlurEffect(blurRadius, blurRadius, Shader.TileMode.CLAMP)
        )
    }

    fun applyFadingTint() {

    }
}