package com.makinu.app.scheduler.utils

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.makinu.app.scheduler.data.local.db.AppInfoDao
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class LocalService : Service() {

    @Inject
    lateinit var dao: AppInfoDao

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.v("LocalService", "onStartCommand")

        val pm = packageManager
        if (pm != null) {
            val fireIntent = pm.getLaunchIntentForPackage("com.android.chrome")
            if (fireIntent != null) {
                fireIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(fireIntent)
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    // Binder given to clients.
    private val binder = LocalBinder()

    /**
     * Class used for the client Binder. Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    inner class LocalBinder : Binder() {
        // Return this instance of LocalService so clients can call public methods.
        fun getService(): LocalService = this@LocalService
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    fun startDatabaseQueryAfterBoot() {
        scope.launch {
            val list = dao.getAll()
            for (appInfo in list) {
                Log.v("LocalService", appInfo.appName)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}