package com.makinu.app.scheduler.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class MyBootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "android.intent.action.BOOT_COMPLETED") {
            // Set the alarm here.
            // Todo, need few more time finish the following task
            // need to access the database to get the scheduled time and status
            // to re activate the schedule the app
        }
    }
}