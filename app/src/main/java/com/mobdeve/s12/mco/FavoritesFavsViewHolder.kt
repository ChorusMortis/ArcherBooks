package com.mobdeve.s12.mco

import android.util.Log
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mobdeve.s12.mco.databinding.ItemFCardBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FavoritesFavsViewHolder(private val viewBinding: ItemFCardBinding): RecyclerView.ViewHolder(viewBinding.root) {
    fun bindData(book: BookModel) {
        Glide.with(viewBinding.root.context)
            .load(book.coverResource)
            .into(viewBinding.itemFvTvCover)
        viewBinding.itemFvTvTitle.text = book.title
        viewBinding.itemFvTvAuthors.text = book.authors.joinToString(", ")
        viewBinding.itemFvTvPubyear.text = book.publishYear

        viewBinding.itemFvIbFavbtn.tag = book.id
        setInitialFavButtonUI(book.id)
//        addListenerFavoriteBtn(book.id)
    }

    /*** Favorite Button Functions ***/
    private fun setInitialFavButtonUI(bookId: String) {
        CoroutineScope(Dispatchers.Main).launch {
            val firestoreHandler = FirestoreHandler.getInstance(viewBinding.root.context)

            val isBookFavorited = firestoreHandler.isBookFavorited(bookId)
            if(isBookFavorited != null && viewBinding.itemFvIbFavbtn.tag == bookId) {
                updateFavButton(isBookFavorited)
            }
            else {
                Log.e("BookDetailsActivity", "There was an error in checking if book was part of current user's favorites in Firestore.")
            }
        }
    }

   fun updateFavButton(newIsBookFavorited : Boolean) {
        if(newIsBookFavorited) {
            viewBinding.itemFvIbFavbtn.setImageResource(R.drawable.icon_favorite_filled)
        } else {
            viewBinding.itemFvIbFavbtn.setImageResource(R.drawable.icon_favorite_outlined)
        }
    }
}