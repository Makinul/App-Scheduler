package com.makinu.app.scheduler.data.repository

import com.makinu.app.scheduler.data.local.db.AppInfoDao
import com.makinu.app.scheduler.data.model.AppInfo
import javax.inject.Inject

class AppInfoRepository @Inject constructor(private val dao: AppInfoDao) {
    fun getUsers(packageName: String): AppInfo? {
        return dao.getAppInfoByPackageName(packageName)
    }

    fun insertUser(item: AppInfo) {
        dao.insert(item)
    }
}