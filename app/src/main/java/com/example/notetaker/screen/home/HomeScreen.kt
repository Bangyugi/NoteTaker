package com.example.notetaker.screen.home

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.notetaker.ad.BannerAdView
import com.example.notetaker.ad.InterstitialAdManager
import com.example.notetaker.ad.NativeAdStyle
import com.example.notetaker.ad.NativeAdComponent
import com.example.notetaker.ad.RewardedAdManager
import com.example.notetaker.ad.findActivity
import com.example.notetaker.screen.common.NoteItem
import com.example.notetaker.screen.navigation.Screen
import kotlin.math.log

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    navController: NavController
) {
    val notes by viewModel.notes.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }



    LaunchedEffect(Unit) {
        InterstitialAdManager.loadAd(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Danh sách ghi chú") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (activity != null) {
                        Toast.makeText(context, "Đang tải quảng cáo, vui lòng đợi...", Toast.LENGTH_SHORT).show()
                        RewardedAdManager.loadAndShowAd(
                            activity = activity,
                            onRewardEarned = {
                                navController.navigate(Screen.NoteScreen.createRoute(-1))
                            },
                            onAdFailedToLoad = { errorMsg ->
                                Toast.makeText(
                                    context,
                                    "Không thể tải quảng cáo: $errorMsg",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            onAdDismissedWithoutReward = {
                                Log.d("HomeScreen", "HomeScreen: Người dùng chưa xem hết ad")
                            }
                        )
                    } else {
                        navController.navigate(Screen.NoteScreen.createRoute(-1))
                    }
                }, shape = CircleShape) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add note",

                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChange(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Tìm kiếm ghi chú ...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Tìm kiếm"
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Xóa tìm kiếm")
                        }
                    } else null
                },
                singleLine = true
            )

            NativeAdComponent(
                style = NativeAdStyle.LARGE,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            if (notes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Không có ghi chú nào")
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(notes) { note ->
                        NoteItem(
                            note = note,
                            onClick = {
                                if(activity != null){
                                    InterstitialAdManager.showAd(
                                        activity = activity,
                                        onAdDismissed = {
                                            navController.navigate(Screen.NoteScreen.createRoute(note.id))
                                        }
                                    )
                                }
                                else{
                                    navController.navigate(Screen.NoteScreen.createRoute(note.id))
                                }
                            },
                            onDeleteClick = {
                                viewModel.deleteNote(note)
                            }
                        )
                    }
                }
            }
            BannerAdView()
        }
    }
}