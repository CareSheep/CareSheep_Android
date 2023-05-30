package com.swu.caresheep

class Breakfast {

    var done: Int? = null
    var user_id: Int? = null
    var date: String? = null


    constructor(
        done: Int?,
        user_id: Int?,
        date: String?,
    ) {
        this.done = done
        this.user_id = user_id
        this.date = date
    }
}