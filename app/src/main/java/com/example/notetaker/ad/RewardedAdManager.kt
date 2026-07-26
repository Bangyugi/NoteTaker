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

class RewardedAdManager {
    private var rewardedAd: RewardedAd? = null
    private var isLoading = false

    fun loadAd(context: Context) {
        if (rewardedAd != null || isLoading) return
        isLoading = true

        val adRequest = AdRequest.Builder(AD_UNIT_ID).build()
        RewardedAd.load(
            adRequest,
            object : AdLoadCallback<RewardedAd> {
                override fun onAdLoaded(ad: RewardedAd) {
                    Log.d("RewardedAdManager", "Quảng cáo Rewarded đã tải xong")
                    rewardedAd = ad
                    isLoading = false
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.e("RewardedAdManager", "Lỗi tải quảng cáo: ${adError.message}")
                    rewardedAd = null
                    isLoading = false
                }
            }
        )
    }

    fun showAd(
        activity: Activity,
        onRewardEarned: () -> Unit,
        onAdNotReady: () -> Unit,
        onAdDismissedWithoutReward: () -> Unit = {}
    ) {

        loadAd(activity)

        CoroutineScope(Dispatchers.Main).launch {
            var waitTime = 0
            while (rewardedAd == null && waitTime < 10000) {
                delay(1000)
                waitTime += 1000
            }

            val ad = rewardedAd
            if (ad == null) {
                Log.d("RewardedAdManager", "Quảng cáo chưa sẵn sàng, cho phép qua trang tạo note")
                onAdNotReady()
                loadAd(activity)
                return@launch
            }

            var isEarnedReward = false

            ad.adEventCallback = object : RewardedAdEventCallback {
                override fun onAdShowedFullScreenContent() {
                    Log.d("RewardedAdManager", "Quảng cáo đang hiển thị toàn màn hình")
                }

                override fun onAdDismissedFullScreenContent() {
                    Log.d("RewardedAdManager", "Người dùng đã đóng quảng cáo")
                    rewardedAd = null
                    loadAd(activity)

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
                    rewardedAd = null
                    loadAd(activity)

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
    }


    companion object {
        // Mã đơn vị quảng cáo thử nghiệm từ tài liệu
        const val AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"
    }
}
