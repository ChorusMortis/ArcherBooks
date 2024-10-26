package com.mobdeve.s12.mco

import android.content.res.ColorStateList
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.mobdeve.s12.mco.databinding.ItemForYouCardLightBinding

class HomeForYouViewHolder(private val viewBinding: ItemForYouCardLightBinding): RecyclerView.ViewHolder(viewBinding.root) {

    fun bindData(book: BookModel) {
        this.viewBinding.foryoucardIvCover.setImageResource(book.coverResource)
        this.viewBinding.foryoucardTvTitle.text = book.title

        var authors = ""
        book.authors.forEachIndexed { index, author ->
            authors += if(index == book.authors.size - 1) {
                author
            } else {
                "${author}, "
            }
        }
        this.viewBinding.foryoucardTvAuthors.text = "by $authors"

        if(book.hasTransaction == HasTransaction.NONE || book.hasTransaction == HasTransaction.INACTIVE) {
            this.viewBinding.foryoucardTvStatus.text = "Book Available" // TODO MCO3: Transfer this as an enum class to Transaction class
            this.viewBinding.foryoucardTvStatus.setTextColor(ContextCompat.getColor(viewBinding.root.context, R.color.book_available))
        } else {
            // TODO MCO3: Handle status when transaction is active (Pick up by..., Return by..., Overdue..., Unavailable...)
        }
    }

    fun setFonts() {
        this.viewBinding.foryoucardTvTitle.typeface = ResourcesCompat.getFont(viewBinding.root.context, R.font.sf_ui_text_bold)
        this.viewBinding.foryoucardTvAuthors.typeface = ResourcesCompat.getFont(viewBinding.root.context, R.font.sf_ui_text_regular)
        this.viewBinding.foryoucardBtnBorrow.typeface = ResourcesCompat.getFont(viewBinding.root.context, R.font.sf_ui_text_regular)
        this.viewBinding.foryoucardTvStatus.typeface = ResourcesCompat.getFont(viewBinding.root.context, R.font.sf_ui_text_medium)

    }
}