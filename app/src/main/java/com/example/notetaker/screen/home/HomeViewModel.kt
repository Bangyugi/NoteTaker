package com.example.notetaker.screen.home

import androidx.lifecycle.ViewModel
import com.example.notetaker.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: NoteRepository
): ViewModel(){

}