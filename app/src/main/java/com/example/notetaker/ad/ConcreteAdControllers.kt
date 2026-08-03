package com.example.notetaker.ad

import android.app.Activity
import android.content.Context
import android.util.Log

class InterstitialAdController : AdController {
    override fun loadAd(context: Context) {
        InterstitialAdManager.loadAd(context)
    }

    override fun showAd(activity: Activity, onComplete: () -> Unit) {
        if (isReady(activity)) {
            InterstitialAdManager.showAd(
                activity = activity,
                onAdDismissed = { onComplete() }
            )
        } else {
            Log.d("InterstitialAdController", "Quảng cáo chưa sẵn sàng, bỏ qua hiển thị.")
            onComplete()
        }
    }

    override fun isReady(context: Context): Boolean {
        return InterstitialAdManager.isAdReady()
    }
}

class RewardedAdController : AdController {
    override fun loadAd(context: Context) {
        Log.d("RewardedAdController", "RewardedAd sẵn sàng tải on-demand khi hiển thị.")
    }

    override fun showAd(activity: Activity, onComplete: () -> Unit) {
        RewardedAdManager.loadAndShowAd(
            activity = activity,
            onRewardEarned = {
                Log.d("RewardedAdController", "Người dùng đã nhận phần thưởng!")
                onComplete()
            },
            onAdFailedToLoad = { errorMsg ->
                Log.w("RewardedAdController", "Lỗi tải Rewarded Ad: $errorMsg")
                onComplete()
            },
            onAdDismissedWithoutReward = {
                Log.d("RewardedAdController", "Quảng cáo bị tắt trước khi nhận thưởng.")
                onComplete()
            }
        )
    }

    override fun isReady(context: Context): Boolean {
        return true
    }
}

class AppOpenAdController(
    private val appOpenAdManager: AppOpenAdManager = AppOpenAdManager()
) : AdController {

    override fun loadAd(context: Context) {
        appOpenAdManager.loadAd(context)
    }

    override fun showAd(activity: Activity, onComplete: () -> Unit) {
        appOpenAdManager.showAdIfAvailable(
            activity = activity,
            onShowAdCompleteListener = { onComplete() }
        )
    }

    override fun isReady(context: Context): Boolean {
        return appOpenAdManager.isAdAvailable()
    }
}
