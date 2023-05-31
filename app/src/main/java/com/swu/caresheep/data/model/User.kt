package com.swu.caresheep.data.model

class User {

    var id: Int? = null
    var gender: String? = null
    var username: String? = null
    var age: Int? = null
    var code: String? = null
    var gmail: String? = null

    constructor(
        id: Int?,
        gender: String?,
        username: String?,
        age: Int?,
        code: String?,
        gmail: String?
    ) {
        this.id = id
        this.gender = gender
        this.username = username
        this.age = age
        this.code = code
        this.gmail = gmail
    }

    constructor()
}