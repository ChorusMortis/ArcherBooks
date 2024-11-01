package com.mobdeve.s12.mco

enum class HasTransaction {
    ACTIVE,
    INACTIVE,
    NONE
}

class BookModel(val id: String, val title: String, val authors: ArrayList<String>, val description: String, val publisher: String, val coverResource: Int, val shelfLocation: String, val publishYear: Int, val pageCount: Int, val hasTransaction: HasTransaction) {

}