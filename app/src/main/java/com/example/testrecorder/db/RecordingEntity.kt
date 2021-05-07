package com.example.testrecorder.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recordings")
class RecordingEntity(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Int,

    @ColumnInfo(name = "name")
    var name: String,

    @ColumnInfo(name = "path")
    var path: String,

    @ColumnInfo(name = "duration")
    var duration: String,

    @ColumnInfo(name = "time")
    var time: String,

    @ColumnInfo(name = "caller")
    var caller: String,

    @ColumnInfo(name = "calltype")
    var calltype: String
)



