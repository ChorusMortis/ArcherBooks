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
    var recentlyViewed: List<String> = emptyList()
        private set
    var favorites: ArrayList<String> = arrayListOf()
        private set

    enum class SignUpMethod {
        EMAIL,
        GOOGLE
    }

    // don't delete; used for Firebase toObject method which requires empty constructor
    constructor()

    constructor(userId: String, firstName: String, lastName: String, emailAddress: String, signUpMethod: SignUpMethod, recentlyViewed: List<String> = emptyList()) {
        this.userId = userId
        this.firstName = firstName
        this.lastName = lastName
        this.emailAddress = emailAddress
        this.signUpMethod = signUpMethod
        this.recentlyViewed = recentlyViewed
    }

    constructor(firstName: String, lastName: String, emailAddress: String, signUpMethod: SignUpMethod, recentlyViewed: List<String> = emptyList()) {
        this.firstName = firstName
        this.lastName = lastName
        this.emailAddress = emailAddress
        this.signUpMethod = signUpMethod
        this.recentlyViewed = recentlyViewed
    }
}