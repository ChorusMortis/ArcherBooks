package com.mobdeve.s12.mco

class UserModel {
    var userId: String = "-1"
    var firstName: String
        private set
    var lastName: String
        private set
    var emailAddress: String
        private set
    var signUpMethod: SignUpMethod
        private set

    enum class SignUpMethod {
        EMAIL,
        GOOGLE
    }

    constructor(userId: String, firstName: String, lastName: String, emailAddress: String, signUpMethod: SignUpMethod) {
        this.userId = userId
        this.firstName = firstName
        this.lastName = lastName
        this.emailAddress = emailAddress
        this.signUpMethod = signUpMethod
    }

    constructor(firstName: String, lastName: String, emailAddress: String, signUpMethod: SignUpMethod) {
        this.firstName = firstName
        this.lastName = lastName
        this.emailAddress = emailAddress
        this.signUpMethod = signUpMethod
    }
}