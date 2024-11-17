package com.mobdeve.s12.mco

class UserModel {
    var userId: String = "-1"
        private set
    var firstName: String
        private set
    var lastName: String
        private set
    var emailAddress: String
        private set

    constructor(userId: String, firstName: String, lastName: String, emailAddress: String) {
        this.userId = userId
        this.firstName = firstName
        this.lastName = lastName
        this.emailAddress = emailAddress
    }

    constructor(firstName: String, lastName: String, emailAddress: String) {
        this.firstName = firstName
        this.lastName = lastName
        this.emailAddress = emailAddress
    }
}