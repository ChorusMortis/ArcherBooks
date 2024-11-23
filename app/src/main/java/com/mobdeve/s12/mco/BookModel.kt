package com.mobdeve.s12.mco

class BookModel{

    var id: String = "-1"
    var title: String = ""
        private set
    var authors: ArrayList<String> = arrayListOf()
        private set
    var description: String = ""
        private set
    var publisher: String = ""
        private set
    var coverResource: String = ""
        private set
    var shelfLocation: String = ""
        private set
    var publishYear: String = ""
        private set
    var pageCount: String = ""
        private set
    var hasTransaction: HasTransaction = HasTransaction.NONE

    enum class HasTransaction {
        ACTIVE,
        INACTIVE,
        NONE
    }

    constructor()

    constructor(id: String,
                title: String,
                authors: ArrayList<String>,
                description: String,
                publisher: String,
                coverResource: String,
                shelfLocation: String,
                publishYear: String,
                pageCount: String,
                hasTransaction: HasTransaction = HasTransaction.ACTIVE) {
        this.id = id
        this.title = title
        this.authors = authors
        this.description = description
        this.publisher = publisher
        this.coverResource = coverResource
        this.shelfLocation = shelfLocation
        this.publishYear = publishYear
        this.pageCount = pageCount
        this.hasTransaction = hasTransaction
    }

    override fun equals(other: Any?): Boolean {
        val otherBook = other as BookModel?
        return this.id == otherBook?.id
    }

    override fun hashCode(): Int {
        return 31 * this.id.hashCode()
    }

}