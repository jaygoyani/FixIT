package com.android.fixit.utils

object Constants {
    const val COLLECTION_USERS = "users"
    const val COLLECTION_SERVICES = "services"
    const val COLLECTION_JOBS = "jobs"
    const val COLLECTION_FEEDBACK = "feedbacks"

    const val FOLDER_JOBS = "jobs"

    const val CHANNEL_GENERAL_NAME = "General Notifications"
    const val CHANNEL_GENERAL_ID = "fixit_general_notifications"

    const val DOT = "\u25AA"

    const val MAX_IMAGES = 4

    const val TOPIC_ADMIN = "topic-admin"

    enum class ROLES {
        USER, ADMIN, SERVICE_PROVIDER
    }

    enum class JOB_STATUS {
        PENDING, ACCEPTED, IN_PROGRESS, COMPLETED
    }
}