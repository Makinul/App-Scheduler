package com.makinu.app.scheduler.ui.main.home

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.makinu.app.scheduler.data.Event
import com.makinu.app.scheduler.data.Resource
import com.makinu.app.scheduler.data.local.db.AppInfoDao
import com.makinu.app.scheduler.data.model.AppInfo
import com.makinu.app.scheduler.utils.MyPreference
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject


@HiltViewModel
class HomeViewModel @Inject constructor(
    private val preference: MyPreference,
    private val dao: AppInfoDao
) : ViewModel() {

//    private val _homeContents by lazy { MutableLiveData<Event<Resource<List<CommonContent>>>>() }
//    val homeContents: LiveData<Event<Resource<List<CommonContent>>>>
//        get() = _homeContents
//
//    fun requestHomeContents() = viewModelScope.launch(Dispatchers.IO) {
//        _homeContents.postValue(Event(Resource.loading()))
//        repo.homeContents().collect {
//            it?.let { list ->
//                if (list.isEmpty())
//                    _homeContents.postValue(Event(Resource.error("no data found")))
//                else {
//                    _homeContents.postValue(Event(Resource.success(list)))
//                }
//            } ?: _homeContents.postValue(Event(Resource.error("no data found")))
//        }
//    }
//
//    fun getCurrentUser() = viewModelScope.launch {
//        preference.getUser()
//    }

    private val _appStatus by lazy { MutableLiveData<Event<Resource<List<AppInfo>>>>() }
    val appStatus: LiveData<Event<Resource<List<AppInfo>>>>
        get() = _appStatus

    fun getAppStatus() = viewModelScope.launch(Dispatchers.IO) {
        _appStatus.postValue(Event(Resource.loading()))
        dao.getAllAppStatus().collect {
            if (it.isEmpty())
                _appStatus.postValue(Event(Resource.error("no data found")))
            else {
                _appStatus.postValue(Event(Resource.success(it)))
            }
        }
    }

    fun getQueryApps(context: Context) = viewModelScope.launch(Dispatchers.IO) {
        val mainIntent = Intent(Intent.ACTION_MAIN, null)
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)
        val pm: PackageManager = context.packageManager

        val applicationInfoList = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.queryIntentActivities(
                mainIntent,
                PackageManager.ResolveInfoFlags.of(PackageManager.GET_META_DATA.toLong())
            )
        } else {
            pm.queryIntentActivities(mainIntent, PackageManager.GET_META_DATA)
        }
        for (applicationInfo in applicationInfoList) {
            Log.v("TAG", "App Name: ${applicationInfo.loadLabel(pm)}")
        }
    }

    @SuppressLint("QueryPermissionsNeeded")
    fun getInstalledApps(context: Context) = viewModelScope.launch(Dispatchers.IO) {
        val pm: PackageManager = context.packageManager
        //get a list of installed apps.

        val applicationInfoList = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.getInstalledApplications(PackageManager.ApplicationInfoFlags.of(PackageManager.GET_META_DATA.toLong()))
        } else {
            pm.getInstalledApplications(PackageManager.GET_META_DATA)
        }

//        Log.v("TAG", "applicationInfoList: ${applicationInfoList.size}")
//        val info = pm.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
//        Log.v("TAG", "My App Name: ${pm.getApplicationLabel(info)}")

        var counter = 0
        for (applicationInfo in applicationInfoList) {
            if (applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0) {
                Log.v("TAG", "App Name $counter: ${pm.getApplicationLabel(applicationInfo)}")
                counter++
            }
//            val packageInfo = pm.getPackageInfoCompat(applicationInfo.packageName)
//            Log.v("TAG", "Package Name: ${applicationInfo.packageName}")


//            Log.v("TAG", "Source dir : " + applicationInfo.sourceDir)
//            Log.v(
//                "TAG",
//                "Launch Activity :" + pm.getLaunchIntentForPackage(applicationInfo.packageName)
//            )
        }
        // the getLaunchIntentForPackage returns an intent that you can use with startActivity()
    }

    private fun PackageManager.getPackageInfoCompat(
        packageName: String,
        flags: Int = 0
    ): PackageInfo =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(flags.toLong()))
        } else {
            @Suppress("DEPRECATION") getPackageInfo(packageName, flags)
        }

    @SuppressLint("QueryPermissionsNeeded")
    fun getInstalledPackages(context: Context) = viewModelScope.launch(Dispatchers.IO) {
        val pm: PackageManager = context.packageManager
        //get a list of installed apps.

        val applicationInfoList = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.getInstalledPackages(PackageManager.PackageInfoFlags.of(PackageManager.GET_META_DATA.toLong()))
        } else {
            pm.getInstalledPackages(PackageManager.GET_META_DATA)
        }

        var counter = 0
        for (packageInfo in applicationInfoList) {
//            if (applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0) {
            Log.v("TAG", "App Name $counter: ${pm.getApplicationLabel(packageInfo.applicationInfo)}")
            counter++
//            }
//            val packageInfo = pm.getPackageInfoCompat(applicationInfo.packageName)
//            Log.v("TAG", "Package Name: ${applicationInfo.packageName}")


//            Log.v("TAG", "Source dir : " + applicationInfo.sourceDir)
//            Log.v(
//                "TAG",
//                "Launch Activity :" + pm.getLaunchIntentForPackage(applicationInfo.packageName)
//            )
        }
        // the getLaunchIntentForPackage returns an intent that you can use with startActivity()
    }
}