package com.example.notetaker.ad

import android.content.Context
import android.util.Log
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAd
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdLoadResult
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdLoader
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

object NativeAdManager {

    private const val TAG = "NativeAdManager"
    private const val TEST_NATIVE_AD_UNIT_ID = "ca-app-pub-3940256099942544/1044960115"
    private const val DEFAULT_KEY = "home_native"
    private const val AD_EXPIRATION_TIME_MS: Long = 1 * 3600 * 1000L

    private val adMap = ConcurrentHashMap<String, NativeAd>()
    private val loadingMap = ConcurrentHashMap<String, Boolean>()
    private val loadTimeMap = ConcurrentHashMap<String, Long>()

    fun loadAd(
        context: Context,
        placementKey: String = DEFAULT_KEY,
        adUnitId: String = TEST_NATIVE_AD_UNIT_ID,
        onAdLoaded: ((NativeAd) -> Unit)? = null
    ) {
        if (isAdReady(placementKey)) {
            Log.d(TAG, "[$placementKey] Native ad đã sẵn sàng.")
            getAd(placementKey)?.let { onAdLoaded?.invoke(it) }
            return
        }
        if (loadingMap[placementKey] == true) {
            Log.d(TAG, "[$placementKey] Native ad đang được nạp.")
            return
        }
        loadingMap[placementKey] = true
        Log.d(TAG, "[$placementKey] Đang gửi yêu cầu nạp quảng cáo Native ngầm...")
        CoroutineScope(Dispatchers.IO).launch {
            val request = NativeAdRequest.Builder(
                adUnitId,
                listOf(NativeAd.NativeAdType.NATIVE)
            ).build()
            when (val result = NativeAdLoader.load(request)) {
                is NativeAdLoadResult.NativeAdSuccess -> {
                    Log.d(TAG, "[$placementKey] Preload Native ad thành công.")
                    clearAd(placementKey)
                    adMap[placementKey] = result.ad
                    loadTimeMap[placementKey] = System.currentTimeMillis()
                    loadingMap[placementKey] = false
                    onAdLoaded?.invoke(result.ad)
                }
                is NativeAdLoadResult.Failure -> {
                    Log.e(TAG, "[$placementKey] Lỗi preload Native ad: ${result.error}")
                    loadingMap[placementKey] = false
                }
                else -> {
                    Log.d(TAG, "[$placementKey] Kết quả nạp khác: $result")
                    loadingMap[placementKey] = false
                }
            }
        }
    }

    fun isAdReady(placementKey: String = DEFAULT_KEY): Boolean {
        val ad = adMap[placementKey] ?: return false
        val loadTime = loadTimeMap[placementKey] ?: 0L
        val isExpired = (System.currentTimeMillis() - loadTime) > AD_EXPIRATION_TIME_MS
        if (isExpired) {
            Log.w(TAG, "[$placementKey] Native ad đã quá hạn 1 tiếng. Hủy ad cũ để nạp mới.")
            clearAd(placementKey)
            return false
        }
        return true
    }

    fun getAd(placementKey: String = DEFAULT_KEY): NativeAd? {
        if (!isAdReady(placementKey)) return null
        return adMap[placementKey]
    }

    fun clearAd(placementKey: String) {
        adMap.remove(placementKey)?.destroy()
        loadTimeMap.remove(placementKey)
    }

    fun clearAll() {
        adMap.keys.forEach { key ->
            clearAd(key)
        }
    }
}
