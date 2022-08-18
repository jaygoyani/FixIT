package com.android.fixit.dtos

import com.android.fixit.utils.Constants
import com.google.firebase.firestore.Exclude
import java.io.Serializable
import java.util.*

class UserDTO : Serializable {
    var name: String? = null
    var countryNameCode: String? = null
    var countryCode: String? = null
    var mobileNumber: String? = null
    var emailAddress: String? = null
    var address: String? = null
    var role: String? = null
    var service: String? = null
    var pricePerHr = 0
    var insertedAt: Long? = null
    var jobsClosed = 0

    constructor()

    constructor(
        name: String,
        countryNameCode: String,
        countryCode: String,
        mobile: String,
        email: String,
        address: String,
        role: String,
        service: String,
        price: Int
    ) {
        this.name = name
        this.countryNameCode = countryNameCode
        this.countryCode = countryCode
        this.mobileNumber = mobile
        this.emailAddress = email
        this.address = address
        this.role = role
        this.pricePerHr = price
        this.service = service
        this.insertedAt = Calendar.getInstance().timeInMillis
    }

    constructor(
        name: String,
        countryNameCode: String,
        countryCode: String,
        mobile: String,
        email: String,
        address: String,
        role: String
    ) {
        this.name = name
        this.countryNameCode = countryNameCode
        this.countryCode = countryCode
        this.mobileNumber = mobile
        this.emailAddress = email
        this.address = address
        this.role = role
        this.insertedAt = Calendar.getInstance().timeInMillis
    }

    @Exclude
    fun isAdmin(): Boolean {
        return role == Constants.ROLES.ADMIN.name
    }

    @Exclude
    fun isUser(): Boolean {
        return role == Constants.ROLES.USER.name
    }

    @Exclude
    fun isTechnician(): Boolean {
        return role == Constants.ROLES.SERVICE_PROVIDER.name
    }

    fun updateDetails(
        name: String, countryCode: String, countryNameCode: String, mobile: String,
        email: String, address: String
    ) {
        this.name = name
        this.countryNameCode = countryNameCode
        this.countryCode = countryCode
        this.mobileNumber = mobile
        this.emailAddress = email
        this.address = address
    }

    override fun toString(): String {
        return "$name, Role => $role, $countryCode-$mobileNumber, $emailAddress, $address, $insertedAt"
    }
}