package com.makinu.app.scheduler.ui.main.home

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.PaintDrawable
import android.os.Build
import android.os.UserManager
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.makinu.app.scheduler.data.Event
import com.makinu.app.scheduler.data.Resource
import com.makinu.app.scheduler.data.local.db.AppInfoDao
import com.makinu.app.scheduler.data.model.AppInfo
import com.makinu.app.scheduler.data.model.AppUiInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val dao: AppInfoDao
) : ViewModel() {

    fun setAlarm(appUiInfo: AppUiInfo) = viewModelScope.launch(Dispatchers.IO) {
        var appInfo = dao.getAppInfoByPackageName(appUiInfo.packageName)
        if (appInfo == null) {
            appInfo = AppInfo()
            appInfo.uid = appUiInfo.uid
            appInfo.packageName = appUiInfo.packageName
            appInfo.appName = appUiInfo.appName
            appInfo.scheduleTime = appUiInfo.scheduleTime
            appInfo.isScheduled = true
        } else {
            appInfo.isScheduled = true
            appInfo.successfulScheduledCounter += 1
        }
        dao.insert(appInfo)
    }

    fun cancelAlarm(packageName: String) = viewModelScope.launch(Dispatchers.IO) {
        dao.cancelSchedule(packageName)
    }
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

    fun getLauncherApps(context: Context) = viewModelScope.launch(Dispatchers.IO) {
        _appUiInfos.postValue(Event(Resource.loading()))
        val items = ArrayList<AppUiInfo>()

        val launcherApps: LauncherApps =
            context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
        val userManager = context.getSystemService(Context.USER_SERVICE) as UserManager

        val packageNameRestriction: String? = null
        val profiles = userManager.userProfiles

        for (profile in profiles) {
            val appList = launcherApps.getActivityList(packageNameRestriction, profile)
            for (info in appList) {
                val appInfo = AppUiInfo()
                appInfo.uid = info.applicationInfo.uid
                appInfo.packageName = info.applicationInfo.packageName
                appInfo.appName = info.label.toString()
                appInfo.componentName = info.componentName
                appInfo.userHandle = profile
                appInfo.icon = getBitmapIcon(context, info.getBadgedIcon(0))

                dao.getAppInfoByPackageName(appInfo.packageName)?.let {
                    appInfo.scheduleTime = it.scheduleTime
                    appInfo.isScheduled = it.isScheduled
                }

                items.add(appInfo)
            }
        }

        _appUiInfos.postValue(Event(Resource.success(items)))
    }

    private val _appUiInfos by lazy { MutableLiveData<Event<Resource<List<AppUiInfo>>>>() }
    val appUiInfos: LiveData<Event<Resource<List<AppUiInfo>>>>
        get() = _appUiInfos

    @SuppressLint("QueryPermissionsNeeded")
    fun getAllInstalledAppsUsingQuery(context: Context) = viewModelScope.launch(Dispatchers.IO) {
        _appUiInfos.postValue(Event(Resource.loading()))
        val items = ArrayList<AppUiInfo>()
        val pm: PackageManager = context.packageManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val applicationInfoList = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getInstalledApplications(PackageManager.ApplicationInfoFlags.of(PackageManager.GET_META_DATA.toLong()))
            } else {
                pm.getInstalledApplications(PackageManager.GET_META_DATA)
            }

            for (applicationInfo in applicationInfoList) {
                val appInfo = AppUiInfo()
                appInfo.uid = applicationInfo.uid
                appInfo.packageName = applicationInfo.packageName
                appInfo.appName = pm.getApplicationLabel(applicationInfo).toString()

                appInfo.icon =
                    getBitmapIcon(context, pm.getApplicationIcon(applicationInfo.packageName))

                items.add(appInfo)
            }
        } else {
            val mainIntent = Intent(Intent.ACTION_MAIN, null)
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)
            val resolveInfoList =
                pm.queryIntentActivities(mainIntent, PackageManager.GET_META_DATA)

            for (resolveInfo in resolveInfoList) {
                val appInfo = AppUiInfo()
                appInfo.packageName = resolveInfo.activityInfo.applicationInfo.packageName
                appInfo.appName = resolveInfo.loadLabel(pm).toString()
                appInfo.icon = getBitmapIcon(context, resolveInfo.loadIcon(pm))

                items.add(appInfo)
            }
        }

        _appUiInfos.postValue(Event(Resource.success(items)))
    }

    private fun getBitmapIcon(context: Context, icon: Drawable): Bitmap {
        val width = 100
        val height = 100
        if (icon is PaintDrawable) {
            icon.intrinsicWidth = width
            icon.intrinsicHeight = height
        }

        val c =
            if (icon.opacity != PixelFormat.OPAQUE) Bitmap.Config.ARGB_8888 else Bitmap.Config.RGB_565
        val thumb = Bitmap.createBitmap(width, height, c)
        val canvas = Canvas(thumb)
        canvas.drawFilter = PaintFlagsDrawFilter(Paint.DITHER_FLAG, 0)

        val mOldBounds = Rect()
        mOldBounds.set(icon.bounds)
        icon.setBounds(0, 0, width, height)
        icon.draw(canvas)
        icon.bounds = mOldBounds

        return BitmapDrawable(context.resources, thumb).bitmap
    }

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

        getInstalledApps(context)
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
//            if (applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0) {
            Log.v("TAG", "App Name $counter: ${pm.getApplicationLabel(applicationInfo)}")
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
            Log.v(
                "TAG",
                "App Name $counter: ${pm.getApplicationLabel(packageInfo.applicationInfo)}"
            )
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