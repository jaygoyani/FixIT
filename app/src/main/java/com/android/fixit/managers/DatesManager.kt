package com.android.fixit.managers

import java.text.SimpleDateFormat
import java.util.*

object DatesManager {
    val format1 = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
    val format2 = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
    val format3 = SimpleDateFormat("dd/MM", Locale.ENGLISH)
    private const val tzIndia = "GMT+5:30"
    private const val tzGMT = "GMT"

    const val durationDay = 1000 * 60 * 60 * 24
    const val durationHour = 1000 * 60 * 60
    const val durationMinute = 1000 * 60

    fun getTimeInF3(time: Long): String {
        return format3.format(Date(time))
    }

    fun getCurrentTimeInF1(): String {
        return getTimeInF1(Calendar.getInstance().timeInMillis)
    }

    fun getTimeInF1(time: Long): String {
        return format1.format(Date(time))
    }

    fun getTimeInF2(time: Long): String {
        return if (time != 0L)
            format2.format(Date(time))
        else
            ""
    }

    fun areOnSameDate(t1: Long, t2: Long): Boolean {
        val cal1 = Calendar.getInstance()
        cal1.timeInMillis = t1
        val cal2 = Calendar.getInstance()
        cal2.timeInMillis = t2
        return cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR) &&
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
    }
}