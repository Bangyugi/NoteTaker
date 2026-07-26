package com.example.notetaker.ad

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.libraries.ads.mobile.sdk.appopen.AppOpenAd
import com.google.android.libraries.ads.mobile.sdk.appopen.AppOpenAdEventCallback
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import com.google.android.libraries.ads.mobile.sdk.common.AdRequest
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import java.util.Date

class AppOpenAdManager {
    fun interface OnShowAdCompleteListener {
        fun onShowAdComplete()
    }

    private var appOpenAd: AppOpenAd? = null
    private var isLoadingAd = false
    var isShowingAd = false
    private var loadTime: Long = 0;
    companion object {
        private const val AD_UNIT_ID = "ca-app-pub-3940256099942544/9257395921"
    }

    fun loadAd(context: Context) {
        if (isLoadingAd || isAdAvailable()) {
            Log.d("AppOpenTag", "App open ad is either loading or has already loaded.")
            return
        }
        isLoadingAd = true
        AppOpenAd.load(
            AdRequest.Builder(AD_UNIT_ID).build(),
            object : AdLoadCallback<AppOpenAd> {
                override fun onAdLoaded(ad: AppOpenAd) {
                    appOpenAd = ad
                    isLoadingAd = false
                    loadTime = Date().time
                    Log.d("AppOpenTag", "App open ad loaded.")
                }


                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    isLoadingAd = false
                    Log.w("AppOpenTag", "App open ad failed to load: $loadAdError")
                }
            },
        )
    }


    fun showAdIfAvailable(activity: Activity, onShowAdCompleteListener: OnShowAdCompleteListener?) {
        if (isShowingAd) {
            Log.d("AppOpenTag", "App open ad is already showing.")
            onShowAdCompleteListener?.onShowAdComplete()
            return
        }

        if (!isAdAvailable()) {
            Log.d("AppOpenTag", "App open ad is not ready yet.")
            onShowAdCompleteListener?.onShowAdComplete()
            loadAd(activity)
            return
        }

        appOpenAd?.adEventCallback =
            object : AppOpenAdEventCallback {
                override fun onAdShowedFullScreenContent() {
                    Log.d("AppOpenTag", "App open ad showed.")
                }

                override fun onAdDismissedFullScreenContent() {
                    Log.d("AppOpenTag", "App open ad dismissed.")
                    appOpenAd = null
                    isShowingAd = false
                    onShowAdCompleteListener?.onShowAdComplete()
                    loadAd(activity)
                }

                override fun onAdFailedToShowFullScreenContent(
                    fullScreenContentError: FullScreenContentError
                ) {
                    appOpenAd = null
                    isShowingAd = false
                    Log.w("AppOpenTag", "App open ad failed to show: $fullScreenContentError")
                    onShowAdCompleteListener?.onShowAdComplete()
                    loadAd(activity)
                }

                override fun onAdImpression() {
                    Log.d("AppOpenTag", "App open ad recorded an impression.")
                }

                override fun onAdClicked() {
                    Log.d("AppOpenTag", "App open ad recorded a click.")
                }
            }

        isShowingAd = true
        appOpenAd?.show(activity)
    }

    private fun wasLoadTimeLessThanNHoursAgo(numHours: Long): Boolean {
        val dateDifference: Long = Date().time - loadTime
        val numMilliSecondsPerHour: Long = 3600000
        return dateDifference < numMilliSecondsPerHour * numHours
    }

    /** Check if ad exists and can be shown. */
    private fun isAdAvailable(): Boolean {
        return appOpenAd != null && wasLoadTimeLessThanNHoursAgo(4)
    }

}