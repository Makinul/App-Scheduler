package com.makinu.app.scheduler.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MyForegroundService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    private var appName: String? = null
    private var packageName: String? = null
    private var uid: Int = -1
    private var hour: Int = 0
    private var minute: Int = 0

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        appName = intent?.getStringExtra(AppConstants.KEY_APP_NAME)
        packageName = intent?.getStringExtra(AppConstants.KEY_PACKAGE_NAME)
        uid = intent?.getIntExtra(AppConstants.KEY_APP_UID, -1) ?: -1
        hour = intent?.getIntExtra(AppConstants.KEY_ALARM_HOUR, 0) ?: 0
        minute = intent?.getIntExtra(AppConstants.KEY_ALARM_MINUTE, 0) ?: 0

        val pm = packageManager
        if (pm != null) {
            packageName?.let {
                val launchIntent = pm.getLaunchIntentForPackage(it)
                if (launchIntent != null) {
                    launchIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(launchIntent)
                }
            }
        }
        stopSelf()
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()

        Log.v(
            "MyForegroundService", "onCreate"
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            goToForeground()
        }
//        stopService(this)
    }

    private val serviceNotificationChannelId = "serviceNotificationChannelId"

    @RequiresApi(Build.VERSION_CODES.O)
    fun goToForeground() {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        val serviceChannel = NotificationChannel(
            serviceNotificationChannelId,
            "App Schedule notification service, to launch the scheduled app",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannel(serviceChannel)

        val notification: Notification =
            NotificationCompat.Builder(this, serviceNotificationChannelId)
                .setContentTitle("To Launch the scheduled app")
                .build()

        startForeground(-1, notification)
    }
}