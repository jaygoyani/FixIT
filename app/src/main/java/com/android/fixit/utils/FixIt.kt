package com.android.fixit.utils

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.pm.ActivityInfo
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle

class FixIt : Application() {

    override fun onCreate() {
        context = this
        registerActivityLifecycleCallbacks(object : ActivityLifeCycle() {
            @SuppressLint("SourceLockedOrientationActivity")
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                try {
                    activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }
        })
        createNotificationChannels()
        super.onCreate()
    }

    private fun createNotificationChannels() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            Constants.CHANNEL_GENERAL_ID, Constants.CHANNEL_GENERAL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        )
        channel.setBypassDnd(true)
        channel.enableVibration(true)
        channel.enableLights(true)
        channel.setShowBadge(true)
        channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        val attributes = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .build()
        channel.setSound(
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
            attributes
        )
        manager.createNotificationChannel(channel)
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
        var tag = FixIt::class.java.simpleName
    }

}