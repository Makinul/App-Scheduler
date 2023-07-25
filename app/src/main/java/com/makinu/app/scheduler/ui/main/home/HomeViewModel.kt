package com.makinu.app.scheduler.ui.main.home

import android.content.Context
import android.content.pm.LauncherApps
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.PaintDrawable
import android.os.UserManager
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.makinu.app.scheduler.data.Event
import com.makinu.app.scheduler.data.Resource
import com.makinu.app.scheduler.data.local.db.AppInfoDao
import com.makinu.app.scheduler.data.local.db.SchedulerDao
import com.makinu.app.scheduler.data.model.AppInfo
import com.makinu.app.scheduler.data.model.AppUiInfo
import com.makinu.app.scheduler.data.model.Scheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val appDao: AppInfoDao,
    private val schedulerDao: SchedulerDao
) : ViewModel() {

    private val _schedulers by lazy { MutableLiveData<Event<Resource<List<Scheduler>>>>() }
    val schedulers: LiveData<Event<Resource<List<Scheduler>>>>
        get() = _schedulers

    fun getSchedulers(appUiInfo: AppUiInfo) = viewModelScope.launch(Dispatchers.IO) {
        val items = schedulerDao.getSchedulersByPackageName(appUiInfo.packageName)
        _schedulers.postValue(Event(Resource.success(items)))
    }

    fun setScheduler(scheduler: Scheduler) = viewModelScope.launch(Dispatchers.IO) {
        val item = schedulerDao.insert(scheduler)
        Log.v("setScheduler", item.toString())
    }

    fun deleteScheduler(scheduler: Scheduler) = viewModelScope.launch(Dispatchers.IO) {
        val item = schedulerDao.delete(scheduler)
        Log.v("setScheduler", item.toString())
    }

    fun setAlarm(appUiInfo: AppUiInfo) = viewModelScope.launch(Dispatchers.IO) {
        var appInfo = appDao.getAppInfoByPackageName(appUiInfo.packageName)
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
        appDao.insert(appInfo)
    }

    fun cancelAlarm(packageName: String) = viewModelScope.launch(Dispatchers.IO) {
        appDao.cancelSchedule(packageName)
    }

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

                appDao.getAppInfoByPackageName(appInfo.packageName)?.let {
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
}