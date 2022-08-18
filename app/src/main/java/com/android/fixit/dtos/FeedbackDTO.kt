package com.android.fixit.dtos

import com.android.fixit.managers.PrefManager
import com.android.fixit.utils.Helper
import java.util.*

class FeedbackDTO {
    var subject: String? = null
    var description: String? = null
    var userName: String? = null
    var userMobile: String? = null
    var insertedAt: Long? = null

    constructor() {}

    constructor(subject: String, description: String) {
        this.subject = subject
        this.description = description
        insertedAt = Calendar.getInstance().timeInMillis
        val user = PrefManager.getUserDTO()
        this.userName = user.name
        this.userMobile = Helper.getUserIndex(user)
    }

}