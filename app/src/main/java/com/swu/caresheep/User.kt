package com.swu.caresheep

class User {

    var id: Int
    var gender: String
    var username: String
    var age: Int
    var code: String
    var gmail: String

    constructor(
        id: Int,
        gender: String,
        username: String,
        age: Int,
        code: String,
        gmail: String
    ) {
        this.id = id
        this.gender = gender
        this.username = username
        this.age = age
        this.code = code
        this.gmail = gmail
    }
}