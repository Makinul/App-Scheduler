package com.makinu.app.scheduler.data.model

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity
data class AppUiInfo(
    var packageName: String = "",
    var appName: String = "",
    var icon: Bitmap? = null,
    var scheduleTime: Date = Date(0),
    var isScheduled: Boolean = false
) {
    override fun toString(): String {
        return appName
    }
}