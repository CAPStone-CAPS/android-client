package com.example.capstone_2.DB

import com.example.capstone_2.data.UsageSessionEntity

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.capstone_2.DB.UsageSessionEntity

@Database(entities = [MemoEntity::class, UsageSessionEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun usageSessionDao(): UsageSessionDao
    abstract fun memoDao(): MemoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "capstone_app_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
