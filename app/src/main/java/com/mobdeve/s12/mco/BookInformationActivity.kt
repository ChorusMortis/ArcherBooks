package com.mobdeve.s12.mco

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mobdeve.s12.mco.databinding.ActivityBookInformationBinding

class BookInformationActivity : AppCompatActivity() {

    companion object {
        const val TITLE_KEY = "TITLE_KEY"
        const val YEAR_PUBLISHED_KEY = "YEAR_PUBLISHED_KEY"
        const val AUTHORS_KEY = "AUTHORS_KEY"
        const val COVER_KEY = "COVER_KEY"
        const val STATUS_KEY = "STATUS_KEY"
        const val SUBJECTS_KEY = "SUBJECTS_KEY"
        const val SHELF_LOCATION_KEY = "SHELF_LOCATION_KEY"
    }

    private lateinit var viewBinding : ActivityBookInformationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityBookInformationBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        setDataOnViews()
    }

    private fun setDataOnViews() {
        viewBinding.bookinfoTvBooktitle.text = this.intent.getStringExtra(TITLE_KEY)
        viewBinding.bookinfoTvBookyear.text = this.intent.getStringExtra(YEAR_PUBLISHED_KEY)
        viewBinding.bookinfoTvAuthors.text = this.intent.getStringExtra(AUTHORS_KEY)
        viewBinding.bookinfoIvBookcover.setImageResource(this.intent.getIntExtra(COVER_KEY, R.drawable.book_grinch)) // TODO: Create a default no cover image
        viewBinding.bookinfoTvBookstatus.text = this.intent.getStringExtra(STATUS_KEY)

        val arrSubjects = this.intent.getStringArrayListExtra(SUBJECTS_KEY)
        viewBinding.bookinfoTvSubjects.text = arrSubjects?.let { concatenateStrings(", ", it) }

        viewBinding.bookinfoTvShelflocation.text = this.intent.getStringExtra(SHELF_LOCATION_KEY)
    }

    private fun concatenateStrings(separator: String, strings: ArrayList<String>): String {
        var concatenatedString = ""
        strings.forEachIndexed{index, value ->
            if(index != strings.size - 1) {
                concatenatedString += "${value}${separator}"
            } else {
                concatenatedString += value
            }
        }

        return concatenatedString
    }
}