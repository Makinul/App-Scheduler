package com.makinu.app.scheduler.data.local.db

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.makinu.app.scheduler.data.model.AppInfo
import com.makinu.app.scheduler.data.model.Scheduler

@Database(
    entities = [AppInfo::class, Scheduler::class],
    version = 2
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun treeDao(): AppInfoDao
    abstract fun scheduleDao(): SchedulerDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        // Create and pre-populate the database. See this article for more details:
        // https://medium.com/google-developers/7-pro-tips-for-room-fbadea4bfbd1#4785
        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(context, AppDatabase::class.java, "app_scheduler.sqlite")
                .addMigrations(MIGRATION_1_2)
                .build()
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE TABLE `Scheduler` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `packageName` TEXT NOT NULL, `uid` INTEGER NOT NULL, `scheduleTime` TEXT, `scheduleRunning` INTEGER NOT NULL, `isScheduled` INTEGER NOT NULL)"
                )
            }
        }
    }
}