package com.example.notetaker.ad

import android.app.Activity
import android.content.Context
import android.util.Log
import com.example.notetaker.MainActivity
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import com.google.android.libraries.ads.mobile.sdk.common.AdRequest
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import com.google.android.libraries.ads.mobile.sdk.interstitial.InterstitialAd
import com.google.android.libraries.ads.mobile.sdk.interstitial.InterstitialAdEventCallback

class InterstitialAdManager {
    private var interstitialAd: InterstitialAd? =null
    private var isLoading = false

    companion object {
        private const val AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"
    }

    fun loadAd(context: Context){
        if (interstitialAd != null || isLoading) return
        isLoading = true

        val adRequest = AdRequest.Builder(AD_UNIT_ID).build()

        InterstitialAd.load(
            adRequest,
            object : AdLoadCallback<InterstitialAd>{
                override fun onAdLoaded(ad: InterstitialAd) {
                    Log.d("InterstitialAd","Tải quảng cáo Interstitial thành công")
                    interstitialAd = ad
                    isLoading = false
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.e("InterstitialAd", "Lỗi tải quảng cáo Interstitial:  ${adError.message}")
                    interstitialAd = null
                    isLoading = false
                }
            }
        )
    }

    fun showAd (activity: Activity, onAdDismissed:()-> Unit){
        val ad = interstitialAd
        if (ad==null){
            Log.d("InterstitialAd", "Quảng cáo Interstitial chưa sẵn sàng ")
            onAdDismissed()
            loadAd(activity)
            return
        }
        ad.adEventCallback = object : InterstitialAdEventCallback{
            override fun onAdShowedFullScreenContent() {
                Log.d("InterstitialAd", "Quảng cáo Interstitial đang hiển thị toàn màn hình")
            }

            override fun onAdDismissedFullScreenContent() {
                Log.d("InterstitialAd", "Người dùng đã đóng quảng cáo Interstitial")
                interstitialAd = null
                loadAd(activity)
                activity.runOnUiThread {
                    onAdDismissed()
                }
            }

            override fun onAdFailedToShowFullScreenContent(fullScreenContentError: FullScreenContentError) {
                Log.e("InterstitialAd", "Lỗi hiển thị quảng cáo Interstitial: $fullScreenContentError")
                interstitialAd = null
                loadAd(activity)
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

}