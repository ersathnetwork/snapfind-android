package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ScreenshotEntity::class], version = 1, exportSchema = false)
abstract class SnapFindDatabase : RoomDatabase() {
    abstract fun screenshotDao(): ScreenshotDao

    companion object {
        @Volatile
        private var INSTANCE: SnapFindDatabase? = null

        fun getDatabase(context: Context): SnapFindDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SnapFindDatabase::class.java,
                    "snapfind_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
