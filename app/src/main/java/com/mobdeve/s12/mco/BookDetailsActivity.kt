package com.mobdeve.s12.mco

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mobdeve.s12.mco.databinding.ActivityBookDetailsBinding
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.view.View
import androidx.annotation.RequiresApi


class BookDetailsActivity : AppCompatActivity() {

    companion object {
        const val TITLE_KEY = "TITLE_KEY"
        const val YEAR_PUBLISHED_KEY = "YEAR_PUBLISHED_KEY"
        const val AUTHORS_KEY = "AUTHORS_KEY"
        const val COVER_KEY = "COVER_KEY"
        const val STATUS_KEY = "STATUS_KEY"
        const val PUBLISHER_KEY = "PUBLISHER_KEY"
        const val SHELF_LOCATION_KEY = "SHELF_LOCATION_KEY"
        const val PAGES_KEY = "PAGES_KEY"
        const val DESCRIPTION_KEY = "DESCRIPTION_KEY"
    }

    private lateinit var viewBinding : ActivityBookDetailsBinding

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityBookDetailsBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        setDataOnViews()
        addListenerAndApiLimitBackBtn()
    }

    @RequiresApi(Build.VERSION_CODES.S)
    fun applyBlurToView(view: View, blurRadius: Float) {
        view.setRenderEffect(
            RenderEffect.createBlurEffect(blurRadius, blurRadius, Shader.TileMode.CLAMP)
        )
    }

    private fun setDataOnViews() {
        viewBinding.bookDetailsIvCover.setImageResource(this.intent.getIntExtra(COVER_KEY, R.drawable.book_grinch))
        viewBinding.bookDetailsIvCover2.setImageResource(this.intent.getIntExtra(COVER_KEY, R.drawable.book_grinch))
        viewBinding.bookDetailsTvTitle.text = this.intent.getStringExtra(TITLE_KEY)
        viewBinding.bookDetailsTvAuthors.text = this.intent.getStringExtra(AUTHORS_KEY)
        viewBinding.bookDetailsTvYear.text = this.intent.getIntExtra(YEAR_PUBLISHED_KEY, -1).toString()
        viewBinding.bookDetailsTvShelfLocation.text = this.intent.getStringExtra(SHELF_LOCATION_KEY)
        this.intent.getStringExtra(STATUS_KEY)?.let { setStatusIcon(it) }
        viewBinding.bookDetailsTvStatus.text = this.intent.getStringExtra(STATUS_KEY)
        viewBinding.bookDetailsTvPages.text = "${this.intent.getIntExtra(PAGES_KEY, 0).toString()} pages"
        viewBinding.bookDetailsTvDescription.text = this.intent.getStringExtra(DESCRIPTION_KEY)
    }

    private fun setStatusIcon(status: String) {
        val statusResource : Int = if(status == "Book Available") {
            R.drawable.icon_available
        } else if(status == "Book Unavailable") {
            R.drawable.icon_unavailable
        } else if(status == "Overdue") {
            R.drawable.icon_overdue
        } else {
            R.drawable.icon_timer
        }
        viewBinding.bookDetailsIvStatus.setImageResource(statusResource)
    }

    private fun addListenerAndApiLimitBackBtn() {
        viewBinding.bookDetailsBackBtn.setOnClickListener(View.OnClickListener {
            finish()
        })

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            viewBinding.bookDetailsBackBtn.visibility = View.VISIBLE
        } else {
            viewBinding.bookDetailsBackBtn.visibility = View.GONE
        }
    }

}