package com.example.notetaker.screen.navigation

sealed class Screen (val route: String){
    object HomeScreen: Screen("home_screen")
    object NoteScreen: Screen("note_screen/{noteId}"){
        fun createRoute(noteId: Int = -1): String = "note_screen/$noteId"
    }
}