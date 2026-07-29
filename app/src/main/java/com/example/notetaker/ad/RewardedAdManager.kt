package com.example.notetaker.ad

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import com.google.android.libraries.ads.mobile.sdk.common.AdRequest
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import com.google.android.libraries.ads.mobile.sdk.rewarded.OnUserEarnedRewardListener
import com.google.android.libraries.ads.mobile.sdk.rewarded.RewardItem
import com.google.android.libraries.ads.mobile.sdk.rewarded.RewardedAd
import com.google.android.libraries.ads.mobile.sdk.rewarded.RewardedAdEventCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

object RewardedAdManager {

    private val DEFAULT_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"
    private val DEFAULT_KEY = "rewarded"
    private val AD_EXPIRATION_TIME_MS = 4 * 3600 * 1000L

    private val adMap = ConcurrentHashMap<String, RewardedAd>()
    private val loadingMap = ConcurrentHashMap<String, Boolean>()
    private val loadTimeMap = ConcurrentHashMap<String, Long>()

    fun loadAd(
        context: Context,
        placementKey: String = DEFAULT_KEY,
        adUnitId: String = DEFAULT_AD_UNIT_ID
    ) {
        if (isAdReady(placementKey) || loadingMap[placementKey] == true) {
            Log.d(
                "RewardedAdManager",
                "[$placementKey] Quảng cáo Rewarded đã sẵn sàng hoặc đang tải."
            )
            return
        }

        loadingMap[placementKey] = true
        Log.d("RewardedAdManager", "[$placementKey] Đang gửi yêu cầu nạp Rewarded Ad ngầm...")

        val adRequest = AdRequest.Builder(adUnitId).build()
        RewardedAd.load(
            adRequest,
            object : AdLoadCallback<RewardedAd> {
                override fun onAdLoaded(ad: RewardedAd) {
                    Log.d("RewardedAdManager", "Quảng cáo Rewarded đã tải xong")
                    adMap[placementKey] = ad
                    loadTimeMap[placementKey] = System.currentTimeMillis()
                    loadingMap[placementKey] = false
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.e("RewardedAdManager", "Lỗi tải quảng cáo: ${adError.message}")
                    clearCache(placementKey)
                    loadingMap[placementKey] = false
                }
            }
        )
    }

    fun isAdReady(placementKey: String = DEFAULT_KEY): Boolean {
        val ad = adMap[placementKey] ?: return false
        val loadTime = loadTimeMap[placementKey] ?: 0L
        val isExpired = (System.currentTimeMillis() - loadTime) > AD_EXPIRATION_TIME_MS
        if (isExpired) {
            Log.w("RewardedAdManager", "[$placementKey] Rewarded Ad quá hạn 4h. Đã dọn dẹp cache.")
            clearCache(placementKey)
            return false
        }
        return true
    }

    fun showAd(
        activity: Activity,
        placementKey: String = DEFAULT_KEY,
        adUnitId: String = DEFAULT_AD_UNIT_ID,
        onRewardEarned: () -> Unit,
        onAdNotReady: () -> Unit,
        onAdDismissedWithoutReward: () -> Unit = {}
    ) {

        val ad = adMap[placementKey]

        if (ad == null || !isAdReady(placementKey)) {
            Log.d(
                "RewardedAdManager",
                "[$placementKey] Rewarded Ad chưa sẵn sàng"
            )
            onAdNotReady()
            loadAd(activity.applicationContext, placementKey, adUnitId)
            return
        }

        var isEarnedReward = false

        ad.adEventCallback = object : RewardedAdEventCallback {
            override fun onAdShowedFullScreenContent() {
                Log.d("RewardedAdManager", "Quảng cáo đang hiển thị")
            }

            override fun onAdDismissedFullScreenContent() {
                Log.d("RewardedAdManager", "Người dùng đã đóng quảng cáo")
                clearCache(placementKey)
                loadAd(activity.applicationContext, placementKey, adUnitId)

                activity.runOnUiThread {
                    if (isEarnedReward) {
                        onRewardEarned()
                    } else {
                        onAdDismissedWithoutReward()
                    }
                }
            }

            override fun onAdFailedToShowFullScreenContent(fullScreenContentError: FullScreenContentError) {
                Log.e("RewardedAdManager", "Lỗi hiển thị quảng cáo: $fullScreenContentError")
                clearCache(placementKey)
                loadAd(activity.applicationContext, placementKey, adUnitId)

                activity.runOnUiThread {
                    onAdNotReady()
                }
            }

            override fun onAdImpression() {}
            override fun onAdClicked() {}
        }

        ad.show(activity, object : OnUserEarnedRewardListener {
            override fun onUserEarnedReward(rewardItem: RewardItem) {
                Log.d(
                    "RewardedAdManager",
                    "Đã nhận thưởng: ${rewardItem.amount} ${rewardItem.type}"
                )
                isEarnedReward = true
            }
        })

    }


    private fun clearCache(placementKey: String) {
        adMap.remove(placementKey)
        loadTimeMap.remove(placementKey)
    }
}
