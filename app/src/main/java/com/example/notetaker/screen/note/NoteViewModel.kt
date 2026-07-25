package com.example.notetaker.screen.note

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notetaker.data.NoteEntity
import com.example.notetaker.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.log

@HiltViewModel
class NoteViewModel @Inject constructor(
    private val repository: NoteRepository
): ViewModel() {
    private val _currentNoteId = MutableStateFlow(-1)
    val currentNoteId: StateFlow<Int> = _currentNoteId.asStateFlow()

    private val _note = MutableStateFlow<NoteEntity?>(null)
    val note: StateFlow<NoteEntity?> = _note.asStateFlow()

    val isEditing: Boolean
        get() = _currentNoteId.value > 0

    fun loadNote(noteId: Int){
        _currentNoteId.value = noteId
        if (noteId > 0){
            viewModelScope.launch{
                val entity = repository.getById(noteId)
                _note.value = entity
            }
        }
        else{
            _note.value = NoteEntity(title = "", content = "")
        }
    }

    fun onTitleChange(title: String) {
        _note.value = _note.value?.copy(title = title) ?: NoteEntity(title = title)
    }

    fun onContentChange(content: String) {
        _note.value = _note.value?.copy(content = content) ?: NoteEntity(content = content)
    }

    fun saveNote(onSuccess: () -> Unit = {}){
        viewModelScope.launch{
            val currentNote = _note.value ?: NoteEntity(title = "", content = "")
            if(_currentNoteId.value > 0){
                repository.updateNote(currentNote)
                Log.d("notemodelview", "update note ${_note.value?.id} successfully")

            }
            else {
                repository.insertNote(currentNote)
                Log.d("notemodelview", "add new note ${_note.value?.id} successfully")
            }
            onSuccess()
        }
    }

    fun deleteNote(onSuccess: () -> Unit = {}){
        viewModelScope.launch{
            _note.value?.let {
                repository.deleteNote(it)
            }
            Log.d("notemodelview", "delete note ${_note.value?.id} successfully")
            onSuccess()
        }
    }
}