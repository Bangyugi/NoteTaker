package com.example.notetaker.ad

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import com.google.android.libraries.ads.mobile.sdk.common.AdRequest
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import com.google.android.libraries.ads.mobile.sdk.interstitial.InterstitialAd
import com.google.android.libraries.ads.mobile.sdk.interstitial.InterstitialAdEventCallback
import java.util.concurrent.ConcurrentHashMap

object InterstitialAdManager {

    private val AD_UNIT_ID: String = "ca-app-pub-3940256099942544/1033173712"
    private val KEY: String =  "interstitial"
    private val AD_EXPIRATION_TIME_MS: Long = 4 * 3600 * 1000L

    private val adMap = ConcurrentHashMap<String, InterstitialAd>()

    private val loadingMap = ConcurrentHashMap<String, Boolean>()

    private val loadTimeMap = ConcurrentHashMap<String, Long>()



    fun loadAd(
        context: Context,
        placementKey: String = KEY,
        adUnitId: String = AD_UNIT_ID
    ){
        val appContext = context.applicationContext

        if(isAdReady(placementKey) || loadingMap[placementKey] == true){
            Log.d("InterstitialAd", "[$placementKey] Quảng cáo đã sẵn sàng.")
            return
        }
        loadingMap[placementKey] = true
        Log.d("InterstitialAd", "[$placementKey] Đang gửi yêu cầu nạp quảng cáo ngầm...")



        val adRequest = AdRequest.Builder(adUnitId).build()

        InterstitialAd.load(
            adRequest,
            object : AdLoadCallback<InterstitialAd>{
                override fun onAdLoaded(ad: InterstitialAd) {
                    Log.d("InterstitialAd","Tải quảng cáo Interstitial thành công")
                    adMap[placementKey] = ad
                    loadTimeMap[placementKey] = System.currentTimeMillis()
                    loadingMap[placementKey] = false
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.e("InterstitialAd", "Lỗi tải quảng cáo Interstitial:  ${adError.message}")
                    clearCache(placementKey)
                    loadingMap[placementKey] = false
                }
            }
        )
    }

    fun isAdReady(placementKey: String = KEY): Boolean {
        val ad = adMap[placementKey] ?: return false
        val loadTime = loadTimeMap[placementKey] ?: 0L
        val isExpired = (System.currentTimeMillis() - loadTime) > AD_EXPIRATION_TIME_MS
        if (isExpired) {
            Log.w("InterstitialAd", "[$placementKey] Quảng cáo đã quá hạn 4 tiếng. Tiến hành hủy bỏ cờ cache.")
            clearCache(placementKey)
            return false
        }
        return true
    }

    fun showAd (
        activity: Activity,
        placementKey: String = KEY,
        adUnitId: String = AD_UNIT_ID,
        onAdDismissed:()-> Unit
    ){
        val ad = adMap[placementKey]
        if (ad==null || !isAdReady(placementKey)){
            Log.d("InterstitialAd", "Quảng cáo Interstitial chưa sẵn sàng ")
            onAdDismissed()
            loadAd(activity.applicationContext, placementKey, adUnitId)
            return
        }
        ad.adEventCallback = object : InterstitialAdEventCallback{
            override fun onAdShowedFullScreenContent() {
                Log.d("InterstitialAd", "Quảng cáo Interstitial đang hiển thị")
            }

            override fun onAdDismissedFullScreenContent() {
                Log.d("InterstitialAd", "Người dùng đã đóng quảng cáo Interstitial")
                clearCache(placementKey)
                loadAd(activity.applicationContext, placementKey, adUnitId)
                activity.runOnUiThread { onAdDismissed() }
            }

            override fun onAdFailedToShowFullScreenContent(fullScreenContentError: FullScreenContentError) {
                Log.e("InterstitialAd", "Lỗi hiển thị quảng cáo Interstitial: $fullScreenContentError")
                clearCache(placementKey)
                loadAd(activity.applicationContext, placementKey, adUnitId)
                activity.runOnUiThread {
                    onAdDismissed()
                }
            }
            override fun onAdImpression() {
                Log.d("InterstitialAd", "Quảng cáo Interstitial ghi nhận 1 lượt hiển thị (impression)")
            }
            override fun onAdClicked() {
                Log.d("InterstitialAd", "Người dùng đã click vào quảng cáo Interstitial")
            }
        }

        ad.show(activity)
    }
    private fun clearCache(placementKey: String) {
        adMap.remove(placementKey)
        loadTimeMap.remove(placementKey)
    }
}