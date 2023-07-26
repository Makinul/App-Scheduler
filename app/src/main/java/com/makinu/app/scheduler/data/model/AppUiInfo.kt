package com.makinu.app.scheduler.data.model

import android.content.ComponentName
import android.graphics.Bitmap
import android.graphics.Rect
import android.os.UserHandle

data class AppUiInfo(
    var uid: Int = -1,
    var packageName: String = "",
    var appName: String = "",
    var componentName: ComponentName? = null,
    var userHandle: UserHandle? = null,
    val rect: Rect? = null,
    var icon: Bitmap? = null,
    var scheduleTime: String? = null,
    var isScheduled: Boolean = false,
    var scheduleCounter: Int = 0
) {
    override fun toString(): String {
        return appName
    }
}