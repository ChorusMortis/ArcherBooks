package com.mobdeve.s12.mco

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import kotlin.random.Random

class GoogleBooksAPIHandler {

    suspend fun getBook(bookId: String) : BookModel? {
        val client = OkHttpClient()
        var url = "https://www.googleapis.com/books/v1/volumes/$bookId"

        return withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val json = JSONObject(response.body?.string()!!)
                    Log.d("GoogleBooksAPIHandler getBook", "Successfully retrieved book id $bookId with title = ${json.getString("id")}")

                    val convertedBook = generateBookObject(json)
                    Log.d("GoogleBooksAPIHandler getBook", "Successfully converted book id $bookId to BookModel type")
                    convertedBook
                } else {
                    Log.d("GoogleBooksAPIHandler getBook", "Failed to retrieve response" )
                    null
                }
            } catch(e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    private fun generateBookObject(bookObject : JSONObject) : BookModel {
        val bookVolumeInfo = bookObject.getJSONObject("volumeInfo")

        // Handle Authors
        val authors : ArrayList<String> = arrayListOf()
        val authorsJSONArray = bookVolumeInfo.optJSONArray("authors")
        if(authorsJSONArray != null) {
            for(authorIndex in 0 until authorsJSONArray.length()) {
                authors.add(authorsJSONArray.getString(authorIndex))
            }
        } else {
            authors.add("Unknown Author")
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
        var publishedDate = bookVolumeInfo.optString("publishedDate")
        publishedDate = if(!publishedDate.isNullOrEmpty()) {
            publishedDate.substring(0, 4)
        } else {
            "Unknown Date"
        }

        // Handle Page Count
        var pageCount = bookVolumeInfo.optInt("pageCount").toString()
        if(pageCount == "0") {
            pageCount = "Unknown"
        }

        // Handle Book Cover
        var bookCover = "https://books.google.com/books/publisher/content/images/frontcover/${bookObject.getString("id")}?fife=w640-h960&source=gbs_api"

        return BookModel(
            bookObject.getString("id"),
            bookVolumeInfo.getString("title"),
            authors,
            description,
            publisher,
            bookCover,
            generateRandomShelfLocation(),
            publishedDate,
            pageCount,
            BookModel.HasTransaction.NONE
        )
    }

    suspend fun getBooks(query: String,
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
                    ArrayList()
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
            retrievedBooksArr.add(generateBookObject(bookObject))
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
        return if (query.isBlank()) "" else specifier + formatQueryString(query)
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