package com.makinu.app.scheduler.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Scheduler(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    var packageName: String = "",
    var appName: String = "",
    var uid: Int = -1,
    var scheduleTime: String? = null,
    var scheduleRunning: Boolean = false,
    var isScheduled: Boolean = false
) {
    override fun toString(): String {
        return "$packageName ($id)"
    }
}