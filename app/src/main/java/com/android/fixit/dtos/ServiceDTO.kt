package com.android.fixit.dtos

import com.google.firebase.firestore.Exclude
import java.util.*

class ServiceDTO {
    @Exclude
    var refId: String? = null
    var label: String? = null
    var insertedAt: Long? = null

    constructor()

    constructor(label: String) {
        this.label = label
    }

    init {
        insertedAt = Calendar.getInstance().timeInMillis
    }
}