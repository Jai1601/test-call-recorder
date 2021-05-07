package com.example.testrecorder.db

import androidx.room.*

@Dao
interface RecordingDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecording(recordingEntity: RecordingEntity): Long

    @Query("SELECT * FROM recordings")
    suspend fun get(): List<RecordingEntity>


}












