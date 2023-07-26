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
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.makinu.app.scheduler.R
import com.makinu.app.scheduler.data.local.db.AppDatabase
import com.makinu.app.scheduler.data.local.db.AppInfoDao
import com.makinu.app.scheduler.data.local.db.SchedulerDao
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class ScheduleReceiver : BroadcastReceiver() {

    @Inject
    lateinit var appInfoDao: AppInfoDao

    @Inject
    lateinit var schedulerDao: SchedulerDao

    private val TAG = "ScheduleReceiver"

    /** If the alarm is older than STALE_WINDOW seconds, ignore.  It
     * is probably the result of a time or timezone change  */

    override fun onReceive(context: Context, intent: Intent) {
        val appName = intent.getStringExtra(AppConstants.KEY_APP_NAME)
        val packageName = intent.getStringExtra(AppConstants.KEY_PACKAGE_NAME)
        val id = intent.getIntExtra(AppConstants.KEY_SCHEDULER_ID, -1)
        val uid = intent.getIntExtra(AppConstants.KEY_APP_UID, -1)
        val hour = intent.getIntExtra(AppConstants.KEY_ALARM_HOUR, 0)
        val minute = intent.getIntExtra(AppConstants.KEY_ALARM_MINUTE, 0)

        // to schedule current task again
        if (packageName != null) {
            val coroutineScope = CoroutineScope(Dispatchers.IO)
            coroutineScope.launch {
                if (!::appInfoDao.isInitialized || !::schedulerDao.isInitialized) {
                    val database = AppDatabase.getInstance(context)
                    appInfoDao = database.treeDao()
                    schedulerDao = database.scheduleDao()
                }

//                val appInfo = appInfoDao.getAppInfoByPackageName(packageName)
//                appInfo?.let {
//                    it.successfulScheduledCounter += 1
//                    appInfoDao.update(it)
//                }

                schedulerDao.completeScheduler(id)
            }
        }

        Log.v(
            TAG, "onReceive uid $uid, appName $appName packageName $packageName"
        )

        val serviceIntent = Intent(context, MyForegroundService::class.java)
        serviceIntent.putExtra(AppConstants.KEY_APP_NAME, appName)
        serviceIntent.putExtra(AppConstants.KEY_PACKAGE_NAME, packageName)
        serviceIntent.putExtra(AppConstants.KEY_APP_UID, uid)
        serviceIntent.putExtra(AppConstants.KEY_SCHEDULER_ID, id)
        serviceIntent.putExtra(AppConstants.KEY_ALARM_HOUR, hour)
        serviceIntent.putExtra(AppConstants.KEY_ALARM_MINUTE, minute)

        createNotificationChannel(context)
        generateNotification(context, uid, appName, packageName)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }

    private val appOpeningChannelId = "appOpeningChannelId"

    private fun generateNotification(
        context: Context, uid: Int, appName: String?, packageName: String?
    ) {
        // Create an explicit intent for an Activity in your app
        if (packageName != null) {
            val pm = context.packageManager
            if (pm != null) {
                val fireIntent = pm.getLaunchIntentForPackage(packageName)
                val pendingIntent: PendingIntent =
                    PendingIntent.getActivity(context, uid, fireIntent, PendingIntent.FLAG_ONE_SHOT)

                val builder = NotificationCompat.Builder(context, appOpeningChannelId)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("$appName opening")
                    .setContentText("$appName is going to open now, this notification is just for information purpose.")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    // Set the intent that will fire when the user taps the notification
                    .setContentIntent(pendingIntent).setAutoCancel(true)

                with(NotificationManagerCompat.from(context)) {
                    // notificationId is a unique int for each notification that you must define
                    if (ActivityCompat.checkSelfPermission(
                            context, Manifest.permission.POST_NOTIFICATIONS
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        Toast.makeText(
                            context,
                            "Need notification permission to view which app will going to open",
                            Toast.LENGTH_LONG
                        ).show()
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
            val channelName = "To view the app open state"
            val descriptionText =
                "Need notification permission to view which app will going to open"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(appOpeningChannelId, channelName, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            notificationManager.createNotificationChannel(channel)
        }
    }
}