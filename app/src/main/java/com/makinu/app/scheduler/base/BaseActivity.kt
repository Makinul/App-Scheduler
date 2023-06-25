package com.makinu.app.scheduler.base

import android.Manifest
import android.app.ProgressDialog
import android.content.*
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.makinu.app.scheduler.R
import java.io.IOException

open class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val connectivityManager =
                getSystemService(ConnectivityManager::class.java) as ConnectivityManager
            connectivityManager.registerDefaultNetworkCallback(networkCallback)
        } else {
            val intentFilter = IntentFilter("android.net.conn.CONNECTIVITY_CHANGE")
            registerReceiver(networkChangeReceiver, intentFilter)
        }

        if (isInternetConnected()) {
//            lifecycleScope.launch() {
//                val success = pingOnGoogle()
//                if (success) {
//                    hideNetworkError()
//                } else {
//                    showNetworkError()
//                }
//            }
            hideNetworkError()
        } else {
            showNetworkError()
        }
    }

    private fun pingOnGoogle(): Boolean {
        val command = "ping -c 1 google.com"
        var result = false
        try {
            result = Runtime.getRuntime().exec(command).waitFor() == 0
        } catch (e: InterruptedException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return result
    }

    private fun isInternetConnected(): Boolean {
        var result = false
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val networkCapabilities = connectivityManager.activeNetwork ?: return false
            val actNw =
                connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
            result = when {
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            connectivityManager.run {
                connectivityManager.activeNetworkInfo?.run {
                    result = when (type) {
                        ConnectivityManager.TYPE_WIFI -> true
                        ConnectivityManager.TYPE_MOBILE -> true
                        ConnectivityManager.TYPE_ETHERNET -> true
                        else -> false
                    }
                }
            }
        }

        return result
    }

    private var snackBar: Snackbar? = null

    private fun showNetworkError() {
        hasInternetConnection = false
        val view: View = window.decorView.findViewById(android.R.id.content)
        if (snackBar == null) {
            snackBar = Snackbar.make(
                view,
                R.string.no_network,
                Snackbar.LENGTH_LONG
            )
            snackBar!!.setBackgroundTint(ContextCompat.getColor(this, R.color.default_error))
        }
        showLog("snackBar isShown ${snackBar!!.isShown}")
        snackBar!!.show()
    }

    private fun hideNetworkError() {
        hasInternetConnection = true
        if (snackBar != null)
            snackBar!!.dismiss()
        snackBar = null
    }

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        // network is available for use
        override fun onAvailable(network: Network) {
            super.onAvailable(network)

            showLog("onAvailable")
            hideNetworkError()
        }

        // lost network connection
        override fun onLost(network: Network) {
            super.onLost(network)

            showLog("onLost")
            showNetworkError()
        }
    }

    private val networkChangeReceiver = NetworkChangeReceiver()

    private inner class NetworkChangeReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            if (context == null)
                return

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                val connectivityManager: ConnectivityManager = context
                    .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

                var result = false
                connectivityManager.run {
                    connectivityManager.activeNetworkInfo?.run {
                        result = when (type) {
                            ConnectivityManager.TYPE_WIFI -> true
                            ConnectivityManager.TYPE_MOBILE -> true
                            ConnectivityManager.TYPE_ETHERNET -> true
                            else -> false
                        }
                    }
                }

                if (result) {
                    hideNetworkError()
                } else {
                    showNetworkError()
                }
            }
        }
    }

    fun showSimpleDialog(@StringRes resourceString: Int = R.string.work_in_progress) {
        showSimpleDialog(getString(resourceString))
    }

    fun showSimpleDialog(message: String) {
        val alertDialog = AlertDialog.Builder(this)
            .setMessage(message)
            .setPositiveButton(
                R.string.ok
            ) { _: DialogInterface?, _: Int ->

            }
            .create()
        alertDialog.show()
    }

    fun showLog(@StringRes resourceId: Int) {
        Log.v(TAG, getString(resourceId))
    }

    fun showLog(message: String = getString(R.string.test_log)) {
        Log.v(TAG, message)
    }

    fun showToast(@StringRes resourceId: Int) {
        Toast.makeText(this, getString(resourceId), Toast.LENGTH_SHORT).show()
    }

    fun showToast(message: String = getString(R.string.work_in_progress)) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private var progressDialog: ProgressDialog? = null

    fun getProgressDialog(): ProgressDialog? {
        return progressDialog
    }

    @JvmOverloads
    fun showProgressDialog(message: String = getString(R.string.loading)) {
        showProgressDialog("", message)
    }

    @JvmOverloads
    fun showProgressDialog(title: String?, message: String?, cancelable: Boolean = false) {
        if (progressDialog == null) {
            progressDialog = ProgressDialog.show(this, title, message)
            progressDialog!!.setCancelable(cancelable)
        } else {
            progressDialog!!.setCancelable(cancelable)
            progressDialog!!.setTitle(title)
            progressDialog!!.setMessage(message)
            if (!progressDialog!!.isShowing) {
                progressDialog!!.show()
            }
        }
    }

    fun closeProgressDialog() {
        if (progressDialog != null && progressDialog!!.isShowing) progressDialog!!.dismiss()
        progressDialog = null
    }

    companion object {
        const val TAG = "BaseActivity"
        var hasInternetConnection = false
    }

//    override fun onResume() {
//        super.onResume()
//
//        val notificationManager: NotificationManager =
//            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//        val activeNotifications = notificationManager.activeNotifications
//        for (activeNotification in activeNotifications) {
//            val bundle = activeNotification.notification.contentIntent
//            showLog()
//        }
////        notificationManager.cancelAll()
//    }

    private fun isPermissionGranted(permission: String): Boolean {
        if (ContextCompat.checkSelfPermission(this, permission)
            == PackageManager.PERMISSION_GRANTED
        ) {
            showLog("Permission granted: $permission")
            return true
        }
        showLog("Permission NOT granted: $permission")
        return false
    }

    fun requestCameraPermission(): Boolean {
        val permission = Manifest.permission.CAMERA
        if (isPermissionGranted(permission)) {
            return true
        }
        if (isPermissionBlocked(permission)) {
            goPermissionSettings()
            return false
        }
        requestPermissionLaunchers.launch(
            arrayOf(
                permission
            )
        )
        return false
    }

    fun requestStorageReadPermission(): Boolean {
        val permission = Manifest.permission.READ_EXTERNAL_STORAGE
        if (isPermissionGranted(permission)) {
            return true
        }
        if (isPermissionBlocked(permission)) {
            goPermissionSettings()
            return false
        }
        requestPermissionLaunchers.launch(
            arrayOf(
                permission
            )
        )
        return false
    }

    private fun isPermissionBlocked(permission: String): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(this, permission)
    }

    private fun goPermissionSettings() {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        settingsResultLauncher.launch(intent)
    }

    private val requestPermissionLaunchers = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissions.entries.forEach {
            showLog("${it.key} = ${it.value}")
        }
    }

    var settingsResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            // There are no request codes
            val data: Intent? = result.data
            showLog("registerForActivityResult")
        }

    override fun onDestroy() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            unregisterReceiver(networkChangeReceiver)
        }
        super.onDestroy()
    }
}