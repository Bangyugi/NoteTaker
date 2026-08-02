package com.example.notetaker.ad

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.material3.CircularProgressIndicator
import com.valentinilk.shimmer.shimmer

@Composable
fun BannerAdShimmer(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp)
            .padding(vertical = 4.dp)
            .shimmer(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(50.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.LightGray.copy(alpha = 0.6f))
        )
    }
}

@Composable
fun NativeAdShimmer(
    style: NativeAdStyle,
    modifier: Modifier = Modifier
) {
    val shimmerColor = Color.LightGray.copy(alpha = 0.6f)
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
            .shimmer(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {


            if (style == NativeAdStyle.MEDIUM || style == NativeAdStyle.LARGE) {
                val mediaHeight = if (style == NativeAdStyle.LARGE) 180.dp else 120.dp
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(mediaHeight)
                        .clip(RoundedCornerShape(8.dp))
                        .background(shimmerColor)
                )
            }

            if (style == NativeAdStyle.LARGE) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(12.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(shimmerColor)
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {


                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(shimmerColor)
                )

                Spacer(modifier = Modifier.width(12.dp))


                Column(modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .height(14.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(shimmerColor)
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.4f)
                            .height(10.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(shimmerColor)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(shimmerColor)
            )
        }
    }
}
@Composable
fun RewardedAdLoadingDialog(onDismiss: () -> Unit = {}) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator()
                Text(
                    text = "Đang tải quảng cáo, vui lòng đợi...",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}
