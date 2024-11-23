package com.mobdeve.s12.mco

class UserModel {
    var userId: String = "-1"
    var firstName: String = ""
        private set
    var lastName: String = ""
        private set
    var emailAddress: String = ""
        private set
    var signUpMethod: SignUpMethod = SignUpMethod.EMAIL
        private set
    var recentlyViewed: ArrayList<BookModel> = arrayListOf()
        private set
    var favorites: ArrayList<BookModel> = arrayListOf()
        private set

    enum class SignUpMethod {
        EMAIL,
        GOOGLE
    }

    // don't delete; used for Firebase toObject method which requires empty constructor
    constructor()

    constructor(userId: String, firstName: String, lastName: String, emailAddress: String, signUpMethod: SignUpMethod, recentlyViewed: ArrayList<BookModel> = arrayListOf(), favorites: ArrayList<BookModel> = arrayListOf()) {
        this.userId = userId
        this.firstName = firstName
        this.lastName = lastName
        this.emailAddress = emailAddress
        this.signUpMethod = signUpMethod
        this.recentlyViewed = recentlyViewed
        this.favorites = favorites
    }

    constructor(firstName: String, lastName: String, emailAddress: String, signUpMethod: SignUpMethod, recentlyViewed: ArrayList<BookModel> = arrayListOf(), favorites: ArrayList<BookModel> = arrayListOf()) {
        this.firstName = firstName
        this.lastName = lastName
        this.emailAddress = emailAddress
        this.signUpMethod = signUpMethod
        this.recentlyViewed = recentlyViewed
        this.favorites = favorites
    }
}