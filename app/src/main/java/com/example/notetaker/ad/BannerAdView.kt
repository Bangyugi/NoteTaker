package com.example.notetaker.ad

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContentProviderCompat.requireContext
import com.google.android.libraries.ads.mobile.sdk.banner.AdSize
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAd
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdEventCallback
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdRequest
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadResult
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError
import kotlinx.coroutines.launch

private const val AD_UNIT_ID = "ca-app-pub-3940256099942544/9214589741"

@Composable
fun BannerAdView (
    modifier: Modifier = Modifier,
    showToast: ((String) -> Unit)? = null,

){
    val context = LocalContext.current
    var bannerAdState by remember { mutableStateOf<BannerAd?>(null) }
    val adSize = AdSize.getLargeAnchoredAdaptiveBannerAdSize(context, 360)
    val isPreviewMode = LocalInspectionMode.current

    LaunchedEffect(context) {
        bannerAdState?.destroy()
        if (!isPreviewMode) {

                when (val result = BannerAd.load(BannerAdRequest.Builder(AD_UNIT_ID, adSize).build())) {
                    is AdLoadResult.Success -> {
                        val ad = result.ad
                        ad.adEventCallback =
                            object : BannerAdEventCallback {
                                override fun onAdImpression() {
                                    // Banner ad recorded an impression.
                                    Log.d("BannerAdView", "Banner ad recorded an impression.")
                                }

                                override fun onAdClicked() {
                                    // Banner ad recorded a click.
                                    Log.d("BannerAdView", "Banner ad clicked.")
                                }

                                override fun onAdShowedFullScreenContent() {
                                    // Banner ad showed.
                                    Log.d("BannerAdView", "Banner ad showed full screen content.")
                                }

                                override fun onAdDismissedFullScreenContent() {
                                    // Banner ad dismissed.
                                    Log.d("BannerAdView", "Banner ad dismissed full screen content.")
                                }

                                override fun onAdFailedToShowFullScreenContent(
                                    fullScreenContentError: FullScreenContentError
                                ) {
                                    // Banner ad failed to show.
                                    Log.w("BannerAdView", "Banner ad failed to show full screen content: $fullScreenContentError")
                                }
                            }
                        bannerAdState = result.ad
                    }
                    is AdLoadResult.Failure -> {
                        showToast?.invoke("Banner failed to load.")
                        Log.w("BannerAdView", "Banner ad failed to load: $result.error")
                    }
                }
            }

    }

    if (bannerAdState != null) {
        bannerAdState?.let { bannerAd ->
            Box(
                modifier = modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                AndroidView(
                    modifier = Modifier.wrapContentSize(),
                    factory = { ctx ->
                        val activity = ctx.findActivity() ?: (ctx as Activity)
                        bannerAd.getView(activity)
                    }
                )
            }
        }
    } else {
        BannerAdShimmer(modifier = modifier)
    }



    DisposableEffect(Unit) {
        onDispose {
            bannerAdState?.destroy()
        }
    }





}

fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}