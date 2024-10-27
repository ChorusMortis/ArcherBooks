package com.mobdeve.s12.mco

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.mobdeve.s12.mco.databinding.ActivityMyFavoritesBinding

class MyFavoritesActivity : AppCompatActivity() {
    companion object {
        private const val VERTICAL_SPACE = 24
    }

    private lateinit var myFavoritesBinding : ActivityMyFavoritesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        myFavoritesBinding = ActivityMyFavoritesBinding.inflate(layoutInflater)
        setContentView(myFavoritesBinding.root)

        myFavoritesBinding.myfavsRvFavorites.adapter = MyFavoritesFavsAdapter(BookGenerator.generateSampleBooks())
        myFavoritesBinding.myfavsRvFavorites.layoutManager = LinearLayoutManager(this)
        myFavoritesBinding.myfavsRvFavorites.addItemDecoration(MarginItemDecoration(resources.displayMetrics, VERTICAL_SPACE))
    }
}