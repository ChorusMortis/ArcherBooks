package com.mobdeve.s12.mco

class BookModel(
    val id: String,
    val title: String,
    val authors: ArrayList<String>,
    val description: String,
    val publisher: String,
    val coverResource: Int,
    val shelfLocation: String,
    val publishYear: String,
    val pageCount: Int,
    var hasTransaction: HasTransaction = HasTransaction.ACTIVE) {
    enum class HasTransaction {
        ACTIVE,
        INACTIVE,
        NONE
    }

}