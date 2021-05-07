package com.example.testrecorder

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.example.testrecorder.db.RecordingEntity

class RecordingViewModel @ViewModelInject constructor(
    private val repository: RecordingRepository
): ViewModel() {
    suspend fun getrecording() : List<RecordingEntity> {
        return repository.getRecording()
    }

    suspend fun insertrecording(recordingEntity: RecordingEntity){
        repository.insertRecording(recordingEntity)
    }
}