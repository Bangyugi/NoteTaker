package com.example.notetaker.ad

import android.app.Activity
import android.content.Context

interface InterstitialAdController {
    fun loadAd(context: Context)
    fun isAdReady(): Boolean
    fun showAd(activity: Activity, onAdDismissed: () -> Unit)
}

interface RewardedAdController {
    fun loadAd(context: Context)
    fun isAdReady(): Boolean
    fun showAd(activity: Activity, onUserEarnedReward: () -> Unit, onAdDismissed: () -> Unit)
}

interface AppOpenAdController {
    fun loadAd(context: Context)
    fun isAdReady(): Boolean
    fun showAdIfAvailable(activity: Activity, onShowAdCompleteListener: () -> Unit)
}

interface AdFactory {
    val providerName: String
    fun createInterstitialAdController(): InterstitialAdController
    fun createRewardedAdController(): RewardedAdController
    fun createAppOpenAdController(): AppOpenAdController
}
