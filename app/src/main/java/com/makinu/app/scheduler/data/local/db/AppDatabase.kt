package com.makinu.app.scheduler.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.makinu.app.scheduler.data.model.AppInfo

@Database(
    entities = [AppInfo::class], version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun treeDao(): AppInfoDao

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
                .build()
        }
    }
}