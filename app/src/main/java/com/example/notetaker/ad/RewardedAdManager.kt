package com.example.notetaker.ad

import android.app.Activity
import android.util.Log
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import com.google.android.libraries.ads.mobile.sdk.common.AdRequest
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import com.google.android.libraries.ads.mobile.sdk.rewarded.OnUserEarnedRewardListener
import com.google.android.libraries.ads.mobile.sdk.rewarded.RewardItem
import com.google.android.libraries.ads.mobile.sdk.rewarded.RewardedAd
import com.google.android.libraries.ads.mobile.sdk.rewarded.RewardedAdEventCallback
import java.util.concurrent.ConcurrentHashMap

object RewardedAdManager {

    private const val DEFAULT_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"
    private const val DEFAULT_KEY = "rewarded"

    private val loadingMap = ConcurrentHashMap<String, Boolean>()

    var billingRepository: com.example.notetaker.data.repository.BillingRepository? = null

    fun loadAndShowAd(
        activity: Activity,
        placementKey: String = DEFAULT_KEY,
        adUnitId: String = DEFAULT_AD_UNIT_ID,
        onRewardEarned: () -> Unit,
        onAdFailedToLoad: (String) -> Unit,
        onAdDismissedWithoutReward: () -> Unit = {}
    ) {
        if (billingRepository?.isAdFree?.value == true) {
            Log.d("RewardedAdManager", "App is ad-free. Bypassing RewardedAd.")
            onAdDismissedWithoutReward()
            return
        }
        if (loadingMap[placementKey] == true) {
            Log.d("RewardedAdManager", "[$placementKey] Quảng cáo Rewarded đang được tải, bỏ qua yêu cầu trùng lặp.")
            return
        }

        loadingMap[placementKey] = true
        Log.d("RewardedAdManager", "[$placementKey] Đang yêu cầu tải và hiển thị Rewarded Ad on-demand...")

        val adRequest = AdRequest.Builder(adUnitId).build()
        RewardedAd.load(
            adRequest,
            object : AdLoadCallback<RewardedAd> {
                override fun onAdLoaded(ad: RewardedAd) {
                    Log.d("RewardedAdManager", "[$placementKey] Rewarded Ad đã tải xong, tiến hành hiển thị.")
                    loadingMap[placementKey] = false
                    showLoadedAd(
                        activity = activity,
                        ad = ad,
                        onRewardEarned = onRewardEarned,
                        onAdDismissedWithoutReward = onAdDismissedWithoutReward,
                        onAdFailedToShow = { errorMsg ->
                            onAdFailedToLoad(errorMsg)
                        }
                    )
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.e("RewardedAdManager", "[$placementKey] Lỗi tải quảng cáo: ${adError.message}")
                    loadingMap[placementKey] = false
                    activity.runOnUiThread {
                        onAdFailedToLoad(adError.message)
                    }
                }
            }
        )
    }

    private fun showLoadedAd(
        activity: Activity,
        ad: RewardedAd,
        onRewardEarned: () -> Unit,
        onAdDismissedWithoutReward: () -> Unit,
        onAdFailedToShow: (String) -> Unit
    ) {
        var isEarnedReward = false

        ad.adEventCallback = object : RewardedAdEventCallback {
            override fun onAdShowedFullScreenContent() {
                Log.d("RewardedAdManager", "Quảng cáo Rewarded đang hiển thị toàn màn hình.")
            }

            override fun onAdDismissedFullScreenContent() {
                Log.d("RewardedAdManager", "Người dùng đã đóng quảng cáo Rewarded.")
                activity.runOnUiThread {
                    if (isEarnedReward) {
                        onRewardEarned()
                    } else {
                        onAdDismissedWithoutReward()
                    }
                }
            }

            override fun onAdFailedToShowFullScreenContent(fullScreenContentError: FullScreenContentError) {
                Log.e("RewardedAdManager", "Lỗi hiển thị quảng cáo: ${fullScreenContentError.message}")
                activity.runOnUiThread {
                    onAdFailedToShow(fullScreenContentError.message)
                }
            }

            override fun onAdImpression() {}
            override fun onAdClicked() {}
        }

        ad.show(activity, object : OnUserEarnedRewardListener {
            override fun onUserEarnedReward(rewardItem: RewardItem) {
                Log.d("RewardedAdManager", "Đã nhận thưởng: ${rewardItem.amount} ${rewardItem.type}")
                isEarnedReward = true
            }
        })
    }
}
