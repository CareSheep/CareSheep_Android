package com.swu.caresheep

class GuardianHelperClass {

    var guardian_id: Int? = null
    var user_id: Int? = null
    var guardian_name: String? = null
    var age: Int? = null
    var gender: String? = null
    var code: Int? = null
    var gmail: String? = null

    constructor(
        guardian_id: Int?,
        user_id: Int?,
        guardian_name: String?,
        age: Int?,
        gender: String?,
        code: Int?,
        gmail: String?
    ) {
        this.guardian_id = guardian_id
        this.user_id = user_id
        this.guardian_name = guardian_name
        this.age = age
        this.gender = gender
        this.code = code
        this.gmail = gmail
    }
}