package com.example.notetaker.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "note")
data class NoteEntity (
    @PrimaryKey
    val id: Int,
    @ColumnInfo("Note")
    val note: String?
)