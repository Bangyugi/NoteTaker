package com.example.notetaker.repository

import com.example.notetaker.data.NoteDao
import com.example.notetaker.data.NoteDatabase
import com.example.notetaker.data.NoteEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteRepository @Inject constructor(private val noteDao: NoteDao){

    fun getAllNote(): List<NoteEntity> = noteDao.getAll()
    fun getById(id: Int): NoteEntity = noteDao.getById(id)
    fun getByNote(note: String): List<NoteEntity> = noteDao.getByNote(note)
    suspend fun insertNote(note: NoteEntity) = noteDao.insertNote(note)
    suspend fun updateNote(note: NoteEntity) = noteDao.updateNote(note)
    suspend fun deleteNote(note: NoteEntity) = noteDao.deleteNote(note)
}