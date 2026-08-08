package com.example.notetaker.ad

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.viewinterop.AndroidView
import com.example.notetaker.R
import com.google.android.libraries.ads.mobile.sdk.nativead.MediaView
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAd
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdLoadResult
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdLoader
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdView as SdkNativeAdView

enum class NativeAdStyle {
    SMALL,
    MEDIUM,
    LARGE
}

private const val TEST_NATIVE_AD_UNIT_ID = "ca-app-pub-3940256099942544/1044960115"
@Composable
fun NativeAdComponent(
    modifier: Modifier = Modifier,
    placementKey: String = "home_native",
    adUnitId: String = TEST_NATIVE_AD_UNIT_ID,
    style: NativeAdStyle = NativeAdStyle.SMALL
) {
    val context = LocalContext.current
    val isAdFree = NativeAdManager.billingRepository?.isAdFree?.collectAsState()?.value ?: false
    if (isAdFree) {
        return
    }

    var nativeAd by remember { mutableStateOf<NativeAd?>(NativeAdManager.getAd(placementKey)) }
    val isPreviewMode = LocalInspectionMode.current

    LaunchedEffect(placementKey, adUnitId) {
        if (!isPreviewMode) {
            val fetchAd = {
                NativeAdManager.loadAd(context, placementKey, adUnitId) { loadedAd ->
                    nativeAd = loadedAd
                }
            }
            val cachedAd = NativeAdManager.getAd(placementKey)
            if (cachedAd != null) {
                nativeAd = cachedAd
            } else {
                fetchAd()
            }

            while (isActive) {
                delay(30.seconds)
                NativeAdManager.clearAd(placementKey)
                fetchAd()
            }

        }
    }

    if (nativeAd != null) {
        nativeAd?.let { ad ->
            Box(modifier = modifier.fillMaxWidth()) {
                AndroidView(
                    factory = { ctx ->
                        val layoutId = when (style) {
                            NativeAdStyle.SMALL -> R.layout.native_ad_small
                            NativeAdStyle.MEDIUM -> R.layout.native_ad_medium
                            NativeAdStyle.LARGE -> R.layout.native_ad_large
                        }
                        val themedContext = android.view.ContextThemeWrapper(ctx, R.style.Theme_NoteTaker)
                        val view = LayoutInflater.from(themedContext).inflate(layoutId, null) as SdkNativeAdView
                        populateNativeAdView(ad, view, style)
                        view
                    },
                    update = { view ->
                        populateNativeAdView(ad, view, style)
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    } else {
        NativeAdShimmer(style = style, modifier = modifier)
    }
}

private fun populateNativeAdView(
    nativeAd: NativeAd,
    adView: SdkNativeAdView,
    style: NativeAdStyle
) {
    adView.headlineView = adView.findViewById<TextView>(R.id.ad_headline)?.apply {
        text = nativeAd.headline
    }

    adView.bodyView = adView.findViewById<TextView>(R.id.ad_body)?.apply {
        text = nativeAd.body
        visibility = if (nativeAd.body != null) View.VISIBLE else View.INVISIBLE
    }

    adView.callToActionView = adView.findViewById<Button>(R.id.ad_call_to_action)?.apply {
        text = nativeAd.callToAction
        visibility = if (nativeAd.callToAction != null) View.VISIBLE else View.INVISIBLE
    }

    adView.iconView = adView.findViewById<ImageView>(R.id.ad_app_icon)?.apply {
        if (nativeAd.icon != null) {
            setImageDrawable(nativeAd.icon?.drawable)
            visibility = View.VISIBLE
        } else {
            visibility = View.GONE
        }
    }

    val mediaView = if (style == NativeAdStyle.MEDIUM || style == NativeAdStyle.LARGE) {
        adView.findViewById<MediaView>(R.id.ad_media)
    } else null

    adView.registerNativeAd(nativeAd, mediaView)
}
