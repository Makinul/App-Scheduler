package com.makinu.app.scheduler.utils

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.makinu.app.scheduler.R
import com.makinu.app.scheduler.data.local.db.AppInfoDao
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class ScheduleReceiver : BroadcastReceiver() {

    @Inject
    lateinit var dao: AppInfoDao
    private val TAG = "ScheduleReceiver"

    /** If the alarm is older than STALE_WINDOW seconds, ignore.  It
     * is probably the result of a time or timezone change  */

    override fun onReceive(context: Context, intent: Intent) {
        val appName = intent.getStringExtra(AppConstants.KEY_APP_NAME)
        val packageName = intent.getStringExtra(AppConstants.KEY_PACKAGE_NAME)
        val uid = intent.getIntExtra(AppConstants.KEY_APP_UID, -1)
        val hour = intent.getIntExtra(AppConstants.KEY_ALARM_HOUR, 0)
        val minute = intent.getIntExtra(AppConstants.KEY_ALARM_MINUTE, 0)

//        val localService = Intent(context, LocalService::class.java)
//        ContextCompat.startForegroundService(context, localService)
//        context.startService(localService)

        Log.v(
            TAG, "onReceive uid $uid, appName $appName packageName $packageName"
        )

        if (packageName != null) {
            val coroutineScope = CoroutineScope(Dispatchers.IO)
            coroutineScope.launch {
                val appInfo = dao.getAppInfoByPackageName(packageName)
                appInfo?.let {
                    it.successfulScheduledCounter += 1
                    dao.update(it)
                }
                Log.v(TAG, appInfo?.appName ?: "Not found")
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager

                val alarmIntent = Intent(context, ScheduleReceiver::class.java).let { intent ->
                    intent.putExtra(AppConstants.KEY_APP_UID, uid)
                    intent.putExtra(AppConstants.KEY_APP_NAME, appName)
                    intent.putExtra(AppConstants.KEY_PACKAGE_NAME, packageName)

                    intent.putExtra(AppConstants.KEY_ALARM_HOUR, hour)
                    intent.putExtra(AppConstants.KEY_ALARM_MINUTE, minute)

                    PendingIntent.getBroadcast(
                        context,
                        uid,
                        intent,
                        PendingIntent.FLAG_CANCEL_CURRENT
                    )
                }

                if (alarmIntent != null && alarmManager != null) {
                    alarmManager.cancel(alarmIntent)
                }

                val calendar: Calendar = Calendar.getInstance().apply {
                    timeInMillis = System.currentTimeMillis()

                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                }

                calendar.add(Calendar.DAY_OF_MONTH, 1)

                // to schedule in exact time
                alarmManager?.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    alarmIntent
                )
            }
//            val appInfo = dao.getAppInfoById(packageName)
            val pm = context.packageManager
            if (pm != null) {
                val fireIntent = pm.getLaunchIntentForPackage(packageName)
                if (fireIntent != null) {
                    fireIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    context.startActivity(fireIntent)
                }
            }
        }

        createNotificationChannel(context)
        generateNotification(context, uid, appName, packageName)

        // Todo, need few more time finish the following task
        // need to access the database to get the scheduled time and status
        // to re activate the schedule the app if we don't use interval facility
    }

    private val CHANNEL_ID = "CHANNEL_ID"

    private fun generateNotification(
        context: Context,
        uid: Int,
        appName: String?,
        packageName: String?
    ) {
        // Create an explicit intent for an Activity in your app


        if (packageName != null) {
            val pm = context.packageManager
            if (pm != null) {
                val fireIntent = pm.getLaunchIntentForPackage(packageName)
                val pendingIntent: PendingIntent =
                    PendingIntent.getActivity(context, uid, fireIntent, PendingIntent.FLAG_ONE_SHOT)

                val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(appName)
                    .setContentText(packageName)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    // Set the intent that will fire when the user taps the notification
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)

                with(NotificationManagerCompat.from(context)) {
                    // notificationId is a unique int for each notification that you must define
                    if (ActivityCompat.checkSelfPermission(
                            context,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        Toast.makeText(
                            context,
                            "Need notification post permission",
                            Toast.LENGTH_LONG
                        )
                            .show()
                        return
                    }
                    notify(uid, builder.build())
                }
            }
        }
    }


    private fun createNotificationChannel(context: Context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.channel_name)
            val descriptionText = context.getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            notificationManager.createNotificationChannel(channel)
        }
    }
}