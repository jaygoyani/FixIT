package com.android.fixit.utils

import android.content.ContentResolver
import android.content.Context
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import android.provider.Settings
import android.text.Html
import android.text.Spanned
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.android.fixit.R
import com.android.fixit.dtos.UserDTO
import com.google.firebase.messaging.FirebaseMessaging
import java.io.File

object Helper {
    private var toast: Toast? = null

    fun showToast(context: Context, msg: String) {
        toast?.cancel()
        toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT)
        toast?.show()
    }

    fun isNetworkAvailable(): Boolean {
        var result = false
        val cm =
            FixIt.context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        cm?.run {
            cm.getNetworkCapabilities(cm.activeNetwork)?.run {
                result = when {
                    hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                    hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                    hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                    else -> false
                }
            }
        }
        return result
    }

    fun subscribeToTopic(topic: String?) {
        if (topic != null)
            FirebaseMessaging.getInstance().subscribeToTopic(topic)
    }

    fun unSubscribeToTopic(topic: String?) {
        if (topic != null)
            FirebaseMessaging.getInstance().unsubscribeFromTopic(topic)
    }

    fun getAvatarLink(name: String, bg: String, color: String): String? {
        return "https://ui-avatars.com/api/?name=" + name +
                "&background=" + bg + "&color=" + color + "&size=128&font-size=0.4"
    }

    fun clearCache(context: Context) {
        val path = File(context.externalCacheDir, "camera")
        if (path.exists() && path.isDirectory) {
            for (child in path.listFiles()) {
                child.delete()
            }
        }
    }

    fun getCacheImagePath(context: Context, fileName: String?): Uri? {
        val path = File(context.externalCacheDir, "camera")
        if (!path.exists())
            path.mkdirs()
        val image = File(path, fileName)
        return FileProvider.getUriForFile(context, context.packageName + ".provider", image)
    }

    fun queryName(resolver: ContentResolver, uri: Uri?): String? {
        val returnCursor = resolver.query(uri!!, null, null, null, null)!!
        val nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        returnCursor.moveToFirst()
        val name = returnCursor.getString(nameIndex)
        returnCursor.close()
        return name
    }

    fun getHtmlText(str: String): Spanned {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            Html.fromHtml(str, Html.FROM_HTML_MODE_LEGACY)
        else
            Html.fromHtml(str)
    }

    fun log(msg: String?) {
        Log.d(FixIt.tag, "$msg")
    }

    fun logError(msg: String?) {
        Log.e(FixIt.tag, "$msg")
    }

    fun getUserIndex(user: UserDTO): String {
        return getUserIndex(user.countryCode!!, user.mobileNumber!!)
    }

    fun getTopic(user: UserDTO): String {
        return getUserIndex(user).substring(1)
    }

    fun getUserIndex(code: String, mobile: String): String {
        var index = "$code$mobile"
        if (!index.startsWith("+"))
            index = "+$index"
        return index
    }

    fun formatRole(role: String?): String {
        if (role == null)
            return "N/A"
        return role.replace("_", " ")
    }

    fun getStatusDetails(status: String): Pair<String, Drawable> {
        return when (status) {
            Constants.JOB_STATUS.COMPLETED.name ->
                Pair(
                    Constants.JOB_STATUS.COMPLETED.name,
                    ContextCompat.getDrawable(FixIt.context, R.drawable.bg_green_solid_rect)!!
                )
            Constants.JOB_STATUS.ACCEPTED .name ->
                Pair(
                    Constants.JOB_STATUS.ACCEPTED.name,
                    ContextCompat.getDrawable(FixIt.context, R.drawable.bg_orange_solid_rect)!!
                )
            Constants.JOB_STATUS.IN_PROGRESS.name ->
                Pair(
                    "IN PROGRESS",
                    ContextCompat.getDrawable(FixIt.context, R.drawable.bg_orange_solid_rect)!!
                )
            else ->
                Pair(
                    Constants.JOB_STATUS.PENDING.name,
                    ContextCompat.getDrawable(FixIt.context, R.drawable.bg_red_solid_rect)!!
                )
        }
    }

}