package com.android.fixit.activities

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.android.fixit.BuildConfig
import com.android.fixit.R
import com.android.fixit.managers.DialogsManager
import com.android.fixit.managers.PrefManager
import com.android.fixit.utils.Navigator
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    private val context = this

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        checkAndSaveFirebaseToken()
        val timer = if (BuildConfig.DEBUG)
            500L
        else
            1500L
        Handler(Looper.getMainLooper()).postDelayed({
            proceed()
        }, timer)
    }

    private fun checkAndSaveFirebaseToken() {
        if (PrefManager.getAppStringData(PrefManager.keyFirebaseToken) == null) {
            FirebaseMessaging.getInstance().token.addOnCompleteListener {
                if (it.isSuccessful)
                    PrefManager.saveAppData(PrefManager.keyFirebaseToken, it.result)
            }
        }
    }

    private fun proceed() {
        if (PrefManager.containsUserKey(PrefManager.keyUserDetails)) {
            val user = PrefManager.getUserDTO()
            when {
                user.isAdmin() -> Navigator.toMainActivity()
                user.isUser() -> Navigator.toUserMainActivity()
                else -> Navigator.toWorkerMainActivity()
            }
        } else
            Navigator.toLoginActivity()
        finish()
    }
}