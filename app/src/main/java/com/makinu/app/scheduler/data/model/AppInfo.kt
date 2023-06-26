package com.makinu.app.scheduler.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class AppInfo(
    @PrimaryKey var packageName: String = "",
    var uid: Int = -1,
    var appName: String = "",
    var scheduleTime: String? = null,
    var isScheduled: Boolean = false,
    var successfulScheduledCounter: Int = 0
) {
    override fun toString(): String {
        return appName
    }
}