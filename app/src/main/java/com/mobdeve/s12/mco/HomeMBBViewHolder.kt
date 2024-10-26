package com.mobdeve.s12.mco

import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.mobdeve.s12.mco.databinding.ItemMbbLightBinding

class HomeMBBViewHolder(private val viewBinding: ItemMbbLightBinding): RecyclerView.ViewHolder(viewBinding.root) {

    fun bindData(book: BookModel) {
        this.viewBinding.mbbIvCover.setImageResource(book.coverResource)
        this.viewBinding.mbbTvTitle.text = book.title

        var authors = ""
        book.authors.forEachIndexed { index, author ->
            authors += if(index == book.authors.size - 1) {
                author
            } else {
                "${author},  "
            }
        }
        this.viewBinding.mbbTvAuthors.text = "by $authors"

//        if(book.hasTransaction == HasTransaction.ACTIVE) { // Sample books are just all inactive at this point, so they are commented out in the meantime
            // TODO MCO3: Check the book in the current user's transactions list and get the transactionStatus under the Transaction class
            this.viewBinding.mbbTvStatus.text = "Return by Oct 3, 2025" // TODO MCO3: Transfer this as an enum class to Transaction class
            this.viewBinding.mbbTvStatus.setTextColor(ContextCompat.getColor(viewBinding.root.context, R.color.book_borrowed))
//        } else {
            // TODO MCO3: Handle status when transaction is active (Pick up by..., Return by..., Overdue..., Unavailable...)
//        }
    }

    fun setFonts() {
        this.viewBinding.mbbTvTitle.typeface = ResourcesCompat.getFont(viewBinding.root.context, R.font.sf_ui_text_bold)
        this.viewBinding.mbbTvAuthors.typeface = ResourcesCompat.getFont(viewBinding.root.context, R.font.sf_ui_text_regular)
        this.viewBinding.mbbTvStatus.typeface = ResourcesCompat.getFont(viewBinding.root.context, R.font.sf_ui_text_medium)

    }
}