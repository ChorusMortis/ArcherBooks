package com.mobdeve.s12.mco

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException

class GoogleBooksAPIHandler {

    suspend fun getBookDetails(query: String,
               sortOption: SearchResultsFragment.SortOption,
               filterOption: SearchResultsFragment.FilterOption,
               startIndex: Int,
               maxResults: Int) : JSONObject? {
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
                    json
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