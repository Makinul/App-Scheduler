package com.makinu.app.scheduler.utils

import java.util.*

object AppConstants {
    const val KEY_SCHEDULER_ID = "id"
    const val KEY_APP_UID = "uid"
    const val KEY_PACKAGE_NAME = "packageName"
    const val KEY_APP_NAME = "appName"
    const val KEY_ALARM_HOUR = "hour"
    const val KEY_ALARM_MINUTE = "minute"
    const val KEY_ICON = "icon"

    fun timeConversion(time: String?): String {
        var scheduleTime = "12:00 AM"
        if (time != null) {
            if (time.contains(":")) {
                val parts = time.split(":")
                if (parts.size > 1) {
                    try {
                        var hour = parts[0].toInt()
                        val minute = parts[1].toInt()

                        val am_pm = if (hour > 11) {
                            hour -= 12
                            "PM"
                        } else {
                            "AM"
                        }
                        if (hour == 0)
                            hour = 12

                        scheduleTime = if (hour < 10) {
                            "0$hour"
                        } else {
                            "$hour"
                        }

                        scheduleTime += if (minute < 10) {
                            ":0$minute"
                        } else {
                            ":$minute"
                        }

                        scheduleTime += " $am_pm"

                        return scheduleTime
                    } catch (e: java.lang.NumberFormatException) {
                        e.printStackTrace()
                    }
                }
            }
        }
        return scheduleTime
    }

    fun timeToCalendar(time: String?): Calendar { // 24 hour time format
        val calendar = Calendar.getInstance()
        if (time != null) {
            if (time.contains(":")) {
                val parts = time.split(":")
                if (parts.size > 1) {
                    try {
                        val hour = parts[0].toInt()
                        val minute = parts[1].toInt()

                        calendar.set(Calendar.HOUR_OF_DAY, hour)
                        calendar.set(Calendar.MINUTE, minute)
                        calendar.set(Calendar.SECOND, 0)
                    } catch (e: java.lang.NumberFormatException) {
                        e.printStackTrace()
                    }
                }
            }
        }
        return calendar
    }
}