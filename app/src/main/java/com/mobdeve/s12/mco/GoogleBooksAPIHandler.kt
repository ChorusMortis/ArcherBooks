package com.mobdeve.s12.mco

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import kotlin.random.Random

class GoogleBooksAPIHandler {

    suspend fun getBookDetails(query: String,
               sortOption: SearchResultsFragment.SortOption,
               filterOption: SearchResultsFragment.FilterOption,
               startIndex: Int,
               maxResults: Int) : ArrayList<BookModel>? {
        val client = OkHttpClient()
        var url = "https://www.googleapis.com/books/v1/volumes?q="
        url += stringifyFilterAndQuery(query, filterOption) + "&" +
                stringifySortingParam(sortOption) + "&" +
                stringifyPagination(startIndex, maxResults)

        return withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val json = JSONObject(response.body?.string()!!)
                    Log.d("GoogleBooksAPIHandler", "Successfully retrieved response with number of items = ${json.getInt("totalItems")}" )

                    val retrievedBooksJSON = json.getJSONArray("items")
                    val convertedBooks = generateBookObjects(retrievedBooksJSON)
                    Log.d("GoogleBooksAPIHandler", "Successfully converted books to BookModel type with size = ${convertedBooks.size}" )
                    convertedBooks
                } else {
                    Log.d("GoogleBooksAPIHandler", "Failed to retrieve response" )
                    null
                }
            } catch(e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    private fun generateBookObjects(retrievedBooksJSON : JSONArray) : ArrayList<BookModel> {
        val retrievedBooksArr : ArrayList<BookModel> = arrayListOf()


        for(index in 0 until retrievedBooksJSON.length()) {
            val bookObject = retrievedBooksJSON.getJSONObject(index)
            val bookVolumeInfo = bookObject.getJSONObject("volumeInfo")

            // Handle Authors
            val authors : ArrayList<String> = arrayListOf()
            val authorsJSONArray = bookVolumeInfo.optJSONArray("authors")
            if(authorsJSONArray != null) {
                for(authorIndex in 0 until authorsJSONArray.length()) {
                    authors.add(authorsJSONArray.getString(authorIndex))
                }
            } else {
                authors.add("Unknown")
            }

            // Handle Description
            var description = bookVolumeInfo.optString("description")
            if(description.isNullOrEmpty()) {
                description = "This book does not have any description yet."
            }

            // Handle Publisher
            var publisher = bookVolumeInfo.optString("publisher")
            if(publisher.isNullOrEmpty()) {
                publisher = "Unknown"
            }

            // Handle Published Date
            val publishedDate = bookVolumeInfo.optString("publishedDate")
            var publishedDateInt = -1
            if(!publishedDate.isNullOrEmpty()) {
                publishedDateInt = publishedDate.substring(0, 4).toInt()
            }

            retrievedBooksArr.add(
                BookModel(
                    bookObject.getString("id"),
                    bookVolumeInfo.getString("title"),
                    authors,
                    description,
                    publisher,
                    R.drawable.book_harry_potter,
                    generateRandomShelfLocation(),
                    publishedDateInt,
                    bookVolumeInfo.optInt("pageCount"),
                    BookModel.HasTransaction.NONE
                )
            )
        }

        return retrievedBooksArr
    }

    private fun generateRandomShelfLocation() : String {
        return getRandomInt(7, 14).toString() + "th Flr. " + getRandomInt(1, 50).toString() + getRandomCapitalLetter()
    }

    private fun getRandomInt(min: Int, max: Int): Int {
        return Random.nextInt(min, max + 1)
    }

    private fun getRandomCapitalLetter(): Char {
        val min = 65
        val max = 90
        val randomCode = Random.nextInt(min, max + 1)
        return randomCode.toChar()
    }

    // Helper Methods
    private fun formatQueryString(query: String) : String {
        return query.lowercase().replace("\\s+".toRegex(), "+")
    }

    private fun stringifyFilterAndQuery(query: String, filterOption: SearchResultsFragment.FilterOption) : String {
        val specifier = when(filterOption) {
            SearchResultsFragment.FilterOption.ALL -> ""
            SearchResultsFragment.FilterOption.TITLE -> "intitle:"
            SearchResultsFragment.FilterOption.AUTHOR -> "inauthor:"
            SearchResultsFragment.FilterOption.PUBLISHER -> "publisher:"
            SearchResultsFragment.FilterOption.SUBJECT -> "subject:"
        }
        return specifier + formatQueryString(query)
    }

    private fun stringifySortingParam(sortOption: SearchResultsFragment.SortOption) : String {
        val specifier = "orderBy="
        val value = when(sortOption) {
            SearchResultsFragment.SortOption.NEWEST -> "newest"
            SearchResultsFragment.SortOption.RELEVANCE -> "relevance"
        }
        return specifier + value
    }

    private fun stringifyPagination(startIndex: Int, maxResults: Int) : String {
        return "startIndex=$startIndex&maxResults=$maxResults"
    }
}