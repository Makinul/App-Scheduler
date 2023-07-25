package com.makinu.app.scheduler.data.local.db

import androidx.room.*
import com.makinu.app.scheduler.data.model.AppInfo
import com.makinu.app.scheduler.data.model.Scheduler
import kotlinx.coroutines.flow.Flow

@Dao
interface SchedulerDao {
    @Query("SELECT * FROM scheduler")
    fun getAll(): List<Scheduler>

    @Query("SELECT * FROM scheduler WHERE packageName = :packageName")
    fun getSchedulersByPackageName(packageName: String): List<AppInfo>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(item: Scheduler)

    @Update
    fun update(item: Scheduler)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(items: List<Scheduler>)

    @Delete
    fun delete(item: Scheduler)

    @Query("DELETE FROM scheduler where packageName = :packageName")
    fun delete(packageName: String)

    @Query("UPDATE scheduler SET isScheduled = 0 where id = :id")
    fun cancelScheduler(id: Int)

    @Query("DELETE FROM scheduler")
    suspend fun deleteAll()

    @Query("SELECT * FROM appinfo")
    fun getAllAppStatus(): Flow<List<AppInfo>>
}