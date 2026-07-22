package com.example.notetaker.screen.note

import androidx.lifecycle.ViewModel
import com.example.notetaker.repository.NoteRepository
import javax.inject.Inject

class NoteViewModel @Inject constructor(
    private val repository: NoteRepository
): ViewModel() {
}