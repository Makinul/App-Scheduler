package com.makinu.app.scheduler.utils

import android.Manifest
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
import com.makinu.app.scheduler.ui.main.MainActivity

class AlarmReceiver : BroadcastReceiver() {
    private val TAG = "AlarmReceiver"

    /** If the alarm is older than STALE_WINDOW seconds, ignore.  It
     * is probably the result of a time or timezone change  */

    override fun onReceive(context: Context, intent: Intent) {
        val appName = intent.getStringExtra(AppConstants.KEY_APP_NAME)
        val packageName = intent.getStringExtra(AppConstants.KEY_PACKAGE_NAME)
        val uid = intent.getIntExtra(AppConstants.KEY_APP_UID, -1)

        Log.v(
            TAG, "onReceive uid $uid, appName $appName packageName $packageName"
        )
        if (packageName != null) {
            val pm = context.packageManager
            if (pm != null) {
                val fireIntent = pm.getLaunchIntentForPackage(packageName)
                if (fireIntent != null)
                    context.startActivity(fireIntent)
            }
        }
        // Wake the device and stay awake until the AlarmAlert intent is
        // handled. Also acquire the screen lock so that if the AlarmAlert
        // activity is paused, it will be resumed.
//        AlarmAlertWakeLock.acquireCpuWakeLock(context)
//        AlarmAlertWakeLock.acquireScreenWakeLock(context)

//        /* Close dialogs and window shade */
//        val i = Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
//        context.sendBroadcast(i)
//        // Decide which activity to start based on the state of the keyguard.
//        var c: Class<*> = AlarmAlert::class.java
//        val km = context.getSystemService(
//            Context.KEYGUARD_SERVICE
//        ) as KeyguardManager
//        if (km.inKeyguardRestrictedInputMode()) {
//            // Use the full screen activity for security.
//            c = AlarmAlertFullScreen::class.java
//        }
//        /* launch UI, explicitly stating that this is not due to user action
//         * so that the current app's notification management is not disturbed */
//        val fireAlarm = Intent(context, c)
//        fireAlarm.putExtra("Alarms.ID", id)
//        fireAlarm.putExtra("Alarms.LABEL", intent.getStringExtra("Alarms.LABEL"))
//        fireAlarm.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_USER_ACTION
//        context.startActivity(fireAlarm)

        createNotificationChannel(context)
        generateNotification(context, uid, appName, packageName)
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
                        Toast.makeText(context, "Need notification post permission", Toast.LENGTH_LONG)
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