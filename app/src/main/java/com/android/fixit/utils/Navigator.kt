package com.android.fixit.utils

import android.content.Intent
import com.android.fixit.activities.*
import com.android.fixit.dtos.JobDTO
import com.android.fixit.dtos.UserDTO

object Navigator {
    fun toLoginActivity() {
        val intent = Intent(FixIt.context, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        FixIt.context.startActivity(intent)
    }

    fun toMainActivity() {
        val intent = Intent(FixIt.context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        FixIt.context.startActivity(intent)
    }

    fun toUserMainActivity() {
        val intent = Intent(FixIt.context, UserMainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        FixIt.context.startActivity(intent)
    }

    fun toWorkerMainActivity() {
        val intent = Intent(FixIt.context, WorkerMainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        FixIt.context.startActivity(intent)
    }

    fun toJobDetailsActivity(job: JobDTO) {
        val intent = Intent(FixIt.context, JobDetailsActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra("job", job)
        }
        FixIt.context.startActivity(intent)
    }

    fun toRegistrationActivity(countryCode: String, number: String) {
        val intent = Intent(FixIt.context, RegistrationActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra("country_code", countryCode)
            putExtra("mobile", number)
        }
        FixIt.context.startActivity(intent)
    }

    fun toAddEditUserActivity(user: UserDTO?) {
        val intent = Intent(FixIt.context, AddEditUser::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        if (user != null)
            intent.putExtra("user", user)
        FixIt.context.startActivity(intent)
    }

    fun toPostJobActivity(worker: UserDTO) {
        val intent = Intent(FixIt.context, PostJobActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra("worker", worker)
        }
        FixIt.context.startActivity(intent)
    }
}