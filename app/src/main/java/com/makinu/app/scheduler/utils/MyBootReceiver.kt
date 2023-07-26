package com.makinu.app.scheduler.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.makinu.app.scheduler.data.local.db.AppDatabase
import com.makinu.app.scheduler.data.local.db.AppInfoDao
import com.makinu.app.scheduler.data.local.db.SchedulerDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

class MyBootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var appInfoDao: AppInfoDao

    @Inject
    lateinit var schedulerDao: SchedulerDao

    private val TAG = "MyBootReceiver"

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "android.intent.action.BOOT_COMPLETED") {
            val coroutineScope = CoroutineScope(Dispatchers.IO)
            coroutineScope.launch {
                if (!::appInfoDao.isInitialized || !::schedulerDao.isInitialized) {
                    val database = AppDatabase.getInstance(context)
                    appInfoDao = database.treeDao()
                    schedulerDao = database.scheduleDao()
                }

                val items = schedulerDao.getRunningSchedulers()
                for (item in items) {
                    val alarmManager =
                        context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager

                    val calendar = AppConstants.timeToCalendar(item.scheduleTime)

                    val alarmIntent = Intent(context, ScheduleReceiver::class.java).let { intent ->
                        intent.putExtra(AppConstants.KEY_SCHEDULER_ID, item.id)
                        intent.putExtra(AppConstants.KEY_APP_UID, item.uid)
                        intent.putExtra(AppConstants.KEY_APP_NAME, item.appName)
                        intent.putExtra(AppConstants.KEY_PACKAGE_NAME, item.packageName)

                        intent.putExtra(
                            AppConstants.KEY_ALARM_HOUR,
                            calendar.get(Calendar.HOUR_OF_DAY)
                        )
                        intent.putExtra(
                            AppConstants.KEY_ALARM_MINUTE,
                            calendar.get(Calendar.MINUTE)
                        )

                        PendingIntent.getBroadcast(
                            context,
                            item.id,
                            intent,
                            PendingIntent.FLAG_CANCEL_CURRENT
                        )
                    }
                    if (alarmIntent != null && alarmManager != null) {
                        alarmManager.cancel(alarmIntent)
                    }

                    // to schedule in exact time
                    alarmManager?.setExact(
                        AlarmManager.RTC_WAKEUP, calendar.timeInMillis, alarmIntent
                    )
                }
            }
        }
    }
}