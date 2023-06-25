package com.makinu.app.scheduler.data.local.db

import androidx.room.*
import com.makinu.app.scheduler.data.model.AppInfo
import kotlinx.coroutines.flow.Flow

@Dao
interface AppInfoDao {
    @Query("SELECT * FROM appinfo")
    fun getAll(): List<AppInfo>

    @Query("SELECT * FROM appinfo WHERE packageName = :packageName LIMIT 1")
    fun getAppInfoById(packageName: String): AppInfo?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(appInfo: AppInfo)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(appInfos: List<AppInfo>)

    @Delete
    fun delete(appInfo: AppInfo)

    @Query("DELETE FROM appinfo where packageName = :packageName")
    fun delete(packageName: String)

    @Query("DELETE FROM appinfo")
    suspend fun deleteAll()

    @Query("SELECT * FROM appinfo")
    fun getAllAppStatus(): Flow<List<AppInfo>>
}