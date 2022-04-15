package com.example.assignment4.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class Note(
    @PrimaryKey(autoGenerate = true) val id: Long,

    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "note") val notes: String,
    @ColumnInfo(name = "lastModified") val lastModified: String
) {
    override fun toString(): String{
        return "${title} (${id})"
    }
}