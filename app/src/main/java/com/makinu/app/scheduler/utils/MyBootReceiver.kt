package com.makinu.app.scheduler.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.makinu.app.scheduler.data.local.db.AppInfoDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

class MyBootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var dao: AppInfoDao
    private val TAG = "MyBootReceiver"

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "android.intent.action.BOOT_COMPLETED") {
//            Log.v(TAG, intent.action!!)
//            Toast.makeText(context, intent.action!!, Toast.LENGTH_LONG).show()
            // Set the alarm here.
            // Todo, need few more time finish the following task
            // need to access the database to get the scheduled time and status
            // to re activate the schedule the app

//            val localService = Intent(context, LocalService::class.java)
//            ContextCompat.startForegroundService(context, localService)

            val coroutineScope = CoroutineScope(Dispatchers.IO)
            coroutineScope.launch {
                val appInfoList = dao.getAll()

                if (appInfoList.isNotEmpty()) {
                    val alarmManager =
                        context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
                    for (appInfo in appInfoList) {
                        Log.v(TAG, appInfo.appName)
                        var hour = 0
                        var minute = 0
                        appInfo.scheduleTime?.let { time ->
                            if (time.contains(":")) {
                                val parts = time.split(":")
                                if (parts.size > 1) {
                                    try {
                                        hour = parts[0].toInt()
                                        minute = parts[1].toInt()
                                    } catch (e: java.lang.NumberFormatException) {
                                        e.printStackTrace()
                                    }
                                }
                            }
                        }

                        val alarmIntent =
                            Intent(context, ScheduleReceiver::class.java).let { intent ->
                                intent.putExtra(AppConstants.KEY_APP_UID, appInfo.uid)
                                intent.putExtra(AppConstants.KEY_APP_NAME, appInfo.appName)
                                intent.putExtra(AppConstants.KEY_PACKAGE_NAME, appInfo.packageName)

                                intent.putExtra(AppConstants.KEY_ALARM_HOUR, hour)
                                intent.putExtra(AppConstants.KEY_ALARM_MINUTE, minute)

                                PendingIntent.getBroadcast(
                                    context,
                                    appInfo.uid,
                                    intent,
                                    PendingIntent.FLAG_CANCEL_CURRENT
                                )
                            }

                        if (alarmIntent != null && alarmManager != null) {
                            alarmManager.cancel(alarmIntent)
                        }

                        val calendar: Calendar = Calendar.getInstance().apply {
                            timeInMillis = System.currentTimeMillis()
                        }

                        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
                        val currentMinute = calendar.get(Calendar.MINUTE)

                        if (currentHour <= hour) {
                            if (currentHour < hour) {
                                calendar.add(Calendar.DAY_OF_MONTH, 1)
                            } else {
                                if (currentMinute < minute) {
                                    calendar.add(Calendar.DAY_OF_MONTH, 1)
                                }
                            }
                        }

                        calendar.set(Calendar.HOUR_OF_DAY, hour)
                        calendar.set(Calendar.MINUTE, minute)

                        // to schedule in exact time
                        alarmManager?.setExact(
                            AlarmManager.RTC_WAKEUP,
                            calendar.timeInMillis,
                            alarmIntent
                        )
                    }
                }
            }
        }
    }

//    override fun onReceive(context: Context, intent: Intent) = goAsync {
//
//    }
}
//
//fun BroadcastReceiver.goAsync(
//    context: CoroutineContext = EmptyCoroutineContext,
//    block: suspend CoroutineScope.() -> Unit
//) {
//    val pendingResult = goAsync()
//    CoroutineScope(SupervisorJob()).launch(context) {
//        try {
//            block()
//        } finally {
//            pendingResult.finish()
//        }
//    }
//}
