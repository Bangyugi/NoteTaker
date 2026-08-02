package com.example.notetaker.ad

import android.app.Activity
import android.util.Log
import android.widget.ImageView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import com.google.android.libraries.ads.mobile.sdk.nativead.MediaView
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAd
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdEventCallback
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdLoader
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdLoaderCallback
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdRequest

private const val AD_UNIT_ID = "ca-app-pub-3940256099942544/1044960115"

@Composable
fun NativeAdview (
    modifier: Modifier = Modifier
){
    val context = LocalContext.current
    var nativeAdState by remember { mutableStateOf<NativeAd?>(null) }
    val isPreviewMode = LocalInspectionMode.current


    LaunchedEffect(context) {
        if (!isPreviewMode && nativeAdState == null) {
            val adRequest = NativeAdRequest
                .Builder(AD_UNIT_ID, listOf(NativeAd.NativeAdType.NATIVE))
                .build()
            val adCallback = object : NativeAdLoaderCallback {
                override fun onNativeAdLoaded(nativeAd: NativeAd) {
                    Log.d("NativeAdView", "Tải Native Ad thành công!")

                    nativeAd.adEventCallback = object : NativeAdEventCallback {
                        override fun onAdShowedFullScreenContent() {
                            Log.d("NativeAdView", "Native ad hiển thị nội dung full screen")
                        }
                        override fun onAdDismissedFullScreenContent() {
                            Log.d("NativeAdView", "Native ad đã bị đóng nội dung full screen")
                        }

                        override fun onAdFailedToShowFullScreenContent(fullScreenContentError: FullScreenContentError) {
                            Log.e("NativeAdView", "Native ad lỗi khi hiển thị full screen: $fullScreenContentError")
                        }
                        override fun onAdImpression() {
                            Log.d("NativeAdView", "Native ad ghi nhận 1 lượt hiển thị (Impression)")
                        }
                        override fun onAdClicked() {
                            Log.d("NativeAdView", "Người dùng nhấp vào Native ad")
                        }
                    }
                    nativeAdState = nativeAd
                }
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.e("NativeAdView", "Tải Native Ad thất bại: ${adError.message}")
                    nativeAdState = null
                }
            }

            NativeAdLoader.load(adRequest, adCallback)
        }
    }
//    nativeAdState?.let { nativeAd ->
//        Box(
//            modifier = modifier.fillMaxWidth(),
//            contentAlignment = Alignment.Center
//        ) {
//            AndroidView(
//                modifier = Modifier.wrapContentSize(),
//                factory = { ctx ->
//                    val activity = ctx.findActivity() ?: (ctx as Activity)
//                    nativeAd.(activity)
//                }
//            )
//        }
//    }


    // chỗ này làm ad xấu quá nên vibe cho nhanh
    nativeAdState?.let { nativeAd ->
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                // 1. Nhãn "Ad" màu vàng ở góc trên trái
                Surface(
                    color = Color(0xFFFDD835),
                    shape = RoundedCornerShape(2.dp)
                ) {
                    Text(
                        text = "Ad",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                // 2. Phần Header (Icon + Tiêu đề + Đánh giá + Mô tả)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    // Icon ứng dụng
                    nativeAd.icon?.drawable?.let { iconDrawable ->
                        AndroidView(
                            factory = { ctx ->
                                ImageView(ctx).apply {
                                    setImageDrawable(iconDrawable)
                                    scaleType = ImageView.ScaleType.FIT_CENTER
                                }
                            },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(6.dp))
                        )
                    }
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp)
                    ) {
                        // Tiêu đề (Headline)
                        nativeAd.headline?.let { headline ->
                            Text(
                                text = headline,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1565C0)
                            )
                        }
                                              // Mô tả (Body)
                        nativeAd.body?.let { body ->
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = body,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.DarkGray
                            )
                        }
                    }
                }
                // 3. Ảnh/Video chính của Quảng cáo bằng MediaView của Google Ads SDK
                nativeAd.mediaContent?.let { mediaContent ->
                    Spacer(modifier = Modifier.height(8.dp))
                    AndroidView(
                        factory = { ctx ->
                            MediaView(ctx)
                        },
                        update = { mediaView ->
                            mediaView.mediaContent = mediaContent
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                // 4. Chân trang (Giá / Nguồn + Nút Install bo tròn)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val priceAndStore = listOfNotNull(nativeAd.price, nativeAd.store)
                        .filter { it.isNotBlank() }
                        .joinToString(" ")
                    Text(
                        text = if (priceAndStore.isNotBlank()) priceAndStore else "Free Google Play",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                    nativeAd.callToAction?.let { ctaText ->
                        Button(
                            onClick = { },
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF283593))
                        ) {
                            Text(
                                text = ctaText,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            nativeAdState?.destroy()
            nativeAdState = null
        }
    }
}