package com.makinu.app.scheduler.data.local.db

import androidx.room.*
import com.makinu.app.scheduler.data.model.AppInfo
import kotlinx.coroutines.flow.Flow

@Dao
interface AppInfoDao {
    @Query("SELECT * FROM appinfo")
    fun getAll(): List<AppInfo>

//    @Query("SELECT * FROM appinfo WHERE memberId = :memberId LIMIT 1")
//    fun getTreeById(memberId: String): AppInfo?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(appInfo: AppInfo)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(appInfos: List<AppInfo>)

    @Delete
    fun delete(appInfo: AppInfo)

//    @Query("DELETE FROM appinfo where memberId = :memberId")
//    fun delete(memberId: String)

    @Query("DELETE FROM appinfo")
    suspend fun deleteAll()

    @Query("SELECT * FROM appinfo")
    fun getAllAppStatus(): Flow<List<AppInfo>>
}