package com.example.notetaker.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface NoteDao {
    @Query("SELECT * FROM note")
    fun getAll(): List<NoteEntity>

    @Query("SELECT * FROM note WHERE id = :id")
    fun getById(id: Int): NoteEntity

    @Query("SELECT * FROM note WHERE note LIKE '%' || :note || '%' ")
    fun getByNote(note:String): List<NoteEntity>

    @Insert
    suspend fun insertNote(note: NoteEntity)

    @Delete
    suspend fun deleteNote(note: NoteEntity)

    @Update
    suspend fun updateNote(note: NoteEntity)

}