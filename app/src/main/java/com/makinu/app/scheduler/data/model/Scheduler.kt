package com.makinu.app.scheduler.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Scheduler(
    @PrimaryKey(autoGenerate = true) var id: Int,
    var packageName: String = "",
    var uid: Int = -1,
    var scheduleTime: String? = null,
    var isScheduled: Boolean = false
) {
    override fun toString(): String {
        return "$packageName ($id)"
    }
}