package com.android.fixit.dtos

import com.android.fixit.managers.PrefManager
import com.android.fixit.utils.Constants
import com.android.fixit.utils.Helper
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties
import java.io.Serializable
import java.util.*
import kotlin.collections.ArrayList

@IgnoreExtraProperties
class JobDTO: Serializable {
    @get:Exclude var id: String? = null
    var title: String? = null
    var description: String? = null
    var type: String? = null
    var userName: String? = null
    var userMobile: String? = null
    var technicianName: String? = null
    var technicianMobile: String? = null
    var price = 0
    var address: String? = null
    var jobDate = 0L
    var status = Constants.JOB_STATUS.PENDING.name
    var images = ArrayList<String>()
    var insertedAt = 0L

    constructor()

    constructor(title: String, description: String, address: String, technician: UserDTO) {
        val user = PrefManager.getUserDTO()
        this.title = title
        this.description = description
        this.address = address
        this.type = technician.service
        technicianName = technician.name
        technicianMobile = Helper.getUserIndex(technician)
        userName = user.name
        userMobile = Helper.getUserIndex(user)
        insertedAt = Calendar.getInstance().timeInMillis
    }

}