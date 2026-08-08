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
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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



    val adFactory: com.example.notetaker.ad.AdFactory = remember {
        com.example.notetaker.ad.RoundRobinAdFactory(
            listOf(
                com.example.notetaker.ad.AdMobAdFactory(),
                com.example.notetaker.ad.MockAdFactory()
            )
        )
    }

    val isAdFree by viewModel.isAdFree.collectAsState()
    var showBillingDialog by remember { mutableStateOf(false) }
    var showAdPromptDialog by remember { mutableStateOf(false) }
    var isLoadingRewardedAd by remember { mutableStateOf(false) }

    if (showBillingDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showBillingDialog = false },
            title = { Text("Giả lập Mua Xóa Quảng Cáo (Billing)") },
            text = {
                Text(
                    if (isAdFree)
                        "Trạng thái hiện tại: Đã Xóa Quảng Cáo (VIP Enabled).\nBạn có muốn Reset để bật lại quảng cáo không?"
                    else
                        "Trạng thái hiện tại: Chưa Mua.\nGiả lập mua gói Xóa Quảng Cáo ($1.99) để tắt tất cả quảng cáo trong App."
                )
            },
            confirmButton = {
                androidx.compose.material3.Button(
                    onClick = {
                        if (isAdFree) {
                            viewModel.resetPurchase()
                            Toast.makeText(context, "Đã Reset trạng thái Quảng cáo!", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.buyRemoveAds()
                            Toast.makeText(context, "Đã kích hoạt Xóa Quảng Cáo (Simulated)! ", Toast.LENGTH_SHORT).show()
                        }
                        showBillingDialog = false
                    }
                ) {
                    Text(if (isAdFree) "Reset Quảng cáo" else "Giả lập Mua ($1.99)")
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(
                    onClick = { showBillingDialog = false }
                ) {
                    Text("Đóng")
                }
            }
        )
    }

    LaunchedEffect(Unit) {
        InterstitialAdManager.loadAd(context)
    }

    if (showAdPromptDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showAdPromptDialog = false },
            title = { Text("Giới hạn ghi chú") },
            text = { Text("Bạn đã đạt giới hạn 5 ghi chú miễn phí. Bạn có muốn xem quảng cáo để tiếp tục tạo ghi chú mới không?") },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        showAdPromptDialog = false
                        if (activity != null) {
                            isLoadingRewardedAd = true
                            RewardedAdManager.loadAndShowAd(
                                activity = activity,
                                onRewardEarned = {
                                    isLoadingRewardedAd = false
                                    navController.navigate(Screen.NoteScreen.createRoute(-1))
                                },
                                onAdFailedToLoad = { errorMsg ->
                                    isLoadingRewardedAd = false
                                    Toast.makeText(
                                        context,
                                        "Không thể tải quảng cáo: $errorMsg",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                onAdDismissedWithoutReward = {
                                    isLoadingRewardedAd = false
                                    Log.d("HomeScreen", "HomeScreen: Người dùng chưa xem hết ad")
                                }
                            )
                        } else {
                            navController.navigate(Screen.NoteScreen.createRoute(-1))
                        }
                    }
                ) {
                    Text("Xem quảng cáo")
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(
                    onClick = { showAdPromptDialog = false }
                ) {
                    Text("Bỏ qua")
                }
            }
        )
    }

    if (isLoadingRewardedAd) {
        com.example.notetaker.ad.RewardedAdLoadingDialog(
            onDismiss = { isLoadingRewardedAd = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Danh sách ghi chú") },
                actions = {
                    IconButton(onClick = { showBillingDialog = true }) {
                        Icon(
                            imageVector = if (isAdFree) Icons.Default.Star else Icons.Default.ShoppingCart,
                            contentDescription = "Remove Ads / VIP",
                            tint = if (isAdFree) androidx.compose.ui.graphics.Color(0xFFFFD700) else androidx.compose.ui.graphics.Color.Unspecified
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (isAdFree || notes.size < 5) {
                        navController.navigate(Screen.NoteScreen.createRoute(-1))
                    } else {
                        showAdPromptDialog = true
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
                                if (activity != null) {
                                    val controller = adFactory.createInterstitialAdController()
                                    Log.d("AdRotation", "==> Factory đã xoay chọn Provider: ${controller.javaClass.simpleName}")
                                    controller.loadAd(context)
                                    controller.showAd(
                                        activity = activity,
                                        onAdDismissed = {
                                            navController.navigate(Screen.NoteScreen.createRoute(note.id))
                                        }
                                    )
                                } else {
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