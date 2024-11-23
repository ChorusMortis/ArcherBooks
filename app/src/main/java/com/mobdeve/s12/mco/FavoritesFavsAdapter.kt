package com.mobdeve.s12.mco

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mobdeve.s12.mco.databinding.ItemFCardBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FavoritesFavsAdapter(private val data: ArrayList<BookModel>,
                           private val onBookRemoved: () -> Unit ): RecyclerView.Adapter<FavoritesFavsViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoritesFavsViewHolder {
        // TODO MCO3: Add on-click listener for Favorite button that will add the book to the user's favorites list

        val itemFavsViewBinding = ItemFCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val favsViewHolder = FavoritesFavsViewHolder(itemFavsViewBinding)
        addListenerCard(favsViewHolder, itemFavsViewBinding)
        addListenerFavoriteBtn(favsViewHolder, itemFavsViewBinding)

        return favsViewHolder
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: FavoritesFavsViewHolder, position: Int) {
        holder.bindData(data[position])
    }

    private fun addListenerCard(holder : FavoritesFavsViewHolder, itemFavsViewBinding: ItemFCardBinding) {
        holder.itemView.setOnClickListener(View.OnClickListener {
            val intent = Intent(holder.itemView.context, BookDetailsActivity::class.java)
            intent.putExtra(BookDetailsActivity.ID_KEY, data[holder.bindingAdapterPosition].id)
            intent.putExtra(BookDetailsActivity.TITLE_KEY, data[holder.bindingAdapterPosition].title)
            intent.putExtra(BookDetailsActivity.YEAR_PUBLISHED_KEY, data[holder.bindingAdapterPosition].publishYear)
            intent.putExtra(BookDetailsActivity.AUTHORS_KEY, data[holder.bindingAdapterPosition].authors.joinToString(", "))
            intent.putExtra(BookDetailsActivity.COVER_KEY, data[holder.bindingAdapterPosition].coverResource)
            intent.putExtra(BookDetailsActivity.PUBLISHER_KEY, data[holder.bindingAdapterPosition].publisher)
            intent.putExtra(BookDetailsActivity.SHELF_LOCATION_KEY, data[holder.bindingAdapterPosition].shelfLocation)
            intent.putExtra(BookDetailsActivity.DESCRIPTION_KEY, data[holder.bindingAdapterPosition].description)
            intent.putExtra(BookDetailsActivity.PAGES_KEY, data[holder.bindingAdapterPosition].pageCount)
            holder.itemView.context.startActivity(intent)
        })
    }

    fun addBooks(newBooks: ArrayList<BookModel>) {
        val startingIndex = data.size
        data.addAll(newBooks)
        notifyItemRangeInserted(startingIndex, newBooks.size)
    }

    fun removeAllBooks() {
        data.clear()
        this.notifyDataSetChanged()
    }

    private fun addListenerFavoriteBtn(holder : FavoritesFavsViewHolder, itemFavsViewBinding: ItemFCardBinding) {
        itemFavsViewBinding.itemFvIbFavbtn.setOnClickListener(View.OnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                val bookId = data[holder.bindingAdapterPosition].id
                val firestoreHandler = FirestoreHandler.getInstance(itemFavsViewBinding.root.context)

                val isBookFavorited = firestoreHandler.isBookFavorited(bookId)
                if(isBookFavorited != null) {
                    if(isBookFavorited == true) {
                        firestoreHandler.removeFromFavorites(bookId)
                    } else if(isBookFavorited == false) {
                        firestoreHandler.addToFavorites(bookId)
                    }
                    if (itemFavsViewBinding.itemFvIbFavbtn.tag == bookId) {
                        holder.updateFavButton(!isBookFavorited)
                        if(isBookFavorited) { // if previously favorited
                            data.removeAt(holder.bindingAdapterPosition)
                            this@FavoritesFavsAdapter.notifyItemRemoved(holder.bindingAdapterPosition)
                            this@FavoritesFavsAdapter.notifyItemRangeChanged(holder.bindingAdapterPosition, data.size)
                            onBookRemoved()
                        }

                    }
                }
                else {
                    Log.e("BookDetailsActivity", "There was an error in checking if book was part of current user's favorites in Firestore.")
                }
            }
        })
    }
}