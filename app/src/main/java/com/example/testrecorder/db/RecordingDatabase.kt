package com.example.testrecorder.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [RecordingEntity::class ], version = 1)
abstract class RecordingDatabase: RoomDatabase() {

    abstract fun recordingDao(): RecordingDao

    companion object{
        val DATABASE_NAME: String = "recording_db"
        @Volatile private var instance: RecordingDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context)= instance ?: synchronized(LOCK){
            instance ?: buildDatabase(context).also { instance = it}
        }

        private fun buildDatabase(context: Context) = Room.databaseBuilder(context,
                RecordingDatabase::class.java, DATABASE_NAME)
                .build()
    }


}