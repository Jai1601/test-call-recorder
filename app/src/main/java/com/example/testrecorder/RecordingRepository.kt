package com.example.testrecorder

import com.example.testrecorder.db.RecordingDao
import com.example.testrecorder.db.RecordingEntity
import javax.inject.Inject

class RecordingRepository @Inject constructor(
    private val recordingDao: RecordingDao
){
    // Other functions from YourDao.kt

    suspend fun getRecording() = recordingDao.get()

    suspend fun insertRecording(recordingEntity: RecordingEntity){
        recordingDao.insertRecording(recordingEntity)
    }
}