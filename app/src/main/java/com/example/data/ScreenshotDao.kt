package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ScreenshotDao {
    @Query("SELECT * FROM screenshots ORDER BY importTimestamp DESC")
    fun getAllScreenshots(): Flow<List<ScreenshotEntity>>

    @Query("SELECT * FROM screenshots WHERE isFavorite = 1 ORDER BY importTimestamp DESC")
    fun getFavoriteScreenshots(): Flow<List<ScreenshotEntity>>

    @Query("SELECT * FROM screenshots WHERE id = :id")
    suspend fun getScreenshotById(id: String): ScreenshotEntity?

    @Query("SELECT * FROM screenshots WHERE contentHash = :hash LIMIT 1")
    suspend fun getByHash(hash: String): ScreenshotEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScreenshot(entity: ScreenshotEntity)

    @Update
    suspend fun updateScreenshot(entity: ScreenshotEntity)

    @Delete
    suspend fun deleteScreenshot(entity: ScreenshotEntity)
}
