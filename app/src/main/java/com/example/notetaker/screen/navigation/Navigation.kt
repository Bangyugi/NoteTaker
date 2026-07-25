package com.example.notetaker.screen.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.notetaker.screen.home.HomeScreen
import com.example.notetaker.screen.note.NoteScreen

@Composable
fun Navigation (
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = Screen.HomeScreen.route,
    ){
        composable(route = Screen.HomeScreen.route){
            HomeScreen(navController = navController)
        }
        composable(
            route = Screen.NoteScreen.route,
            arguments = listOf(
                navArgument("noteId"){
                    type = NavType.IntType
                    defaultValue = -1
                }
            )
        ){
                entry ->
            val noteId = entry.arguments?.getInt("noteId") ?: -1
            NoteScreen(navController = navController,
                noteId = noteId)
        }
    }
}