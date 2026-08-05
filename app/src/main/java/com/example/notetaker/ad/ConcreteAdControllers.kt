package com.example.notetaker.ad

import android.app.Activity
import android.content.Context
import java.util.concurrent.atomic.AtomicInteger

class MockInterstitialAdController : InterstitialAdController {
    private var ready = false

    override fun loadAd(context: Context) {
        ready = true
    }

    override fun isAdReady(): Boolean = ready

    override fun showAd(activity: Activity, onAdDismissed: () -> Unit) {
        ready = false
        onAdDismissed()
    }
}

class MockRewardedAdController : RewardedAdController {
    private var ready = false

    override fun loadAd(context: Context) {
        ready = true
    }

    override fun isAdReady(): Boolean = ready

    override fun showAd(activity: Activity, onUserEarnedReward: () -> Unit, onAdDismissed: () -> Unit) {
        ready = false
        onUserEarnedReward()
        onAdDismissed()
    }
}

class MockAppOpenAdController : AppOpenAdController {
    private var ready = false

    override fun loadAd(context: Context) {
        ready = true
    }

    override fun isAdReady(): Boolean = ready

    override fun showAdIfAvailable(activity: Activity, onShowAdCompleteListener: () -> Unit) {
        ready = false
        onShowAdCompleteListener()
    }
}

class MockAdFactory : AdFactory {
    override val providerName: String = "Mock"

    override fun createInterstitialAdController(): InterstitialAdController = MockInterstitialAdController()
    override fun createRewardedAdController(): RewardedAdController = MockRewardedAdController()
    override fun createAppOpenAdController(): AppOpenAdController = MockAppOpenAdController()
}


class AdMobInterstitialAdController : InterstitialAdController {
    override fun loadAd(context: Context) {
        InterstitialAdManager.loadAd(context)
    }

    override fun isAdReady(): Boolean = InterstitialAdManager.isAdReady()

    override fun showAd(activity: Activity, onAdDismissed: () -> Unit) {
        InterstitialAdManager.showAd(activity, onAdDismissed = onAdDismissed)
    }
}

class AdMobRewardedAdController : RewardedAdController {
    private var isLoaded = false

    override fun loadAd(context: Context) {
        isLoaded = true
    }

    override fun isAdReady(): Boolean = isLoaded

    override fun showAd(activity: Activity, onUserEarnedReward: () -> Unit, onAdDismissed: () -> Unit) {
        RewardedAdManager.loadAndShowAd(
            activity = activity,
            onRewardEarned = onUserEarnedReward,
            onAdFailedToLoad = { onAdDismissed() },
            onAdDismissedWithoutReward = onAdDismissed
        )
        isLoaded = false
    }
}

class AdMobAppOpenAdController : AppOpenAdController {
    private val manager = AppOpenAdManager()

    override fun loadAd(context: Context) {
        manager.loadAd(context)
    }

    override fun isAdReady(): Boolean = manager.isShowingAd

    override fun showAdIfAvailable(activity: Activity, onShowAdCompleteListener: () -> Unit) {
        manager.showAdIfAvailable(activity) {
            onShowAdCompleteListener()
        }
    }
}

class AdMobAdFactory : AdFactory {
    override val providerName: String = "AdMob"

    override fun createInterstitialAdController(): InterstitialAdController = AdMobInterstitialAdController()
    override fun createRewardedAdController(): RewardedAdController = AdMobRewardedAdController()
    override fun createAppOpenAdController(): AppOpenAdController = AdMobAppOpenAdController()
}


class RoundRobinAdFactory(
    private val factories: List<AdFactory>
) : AdFactory {
    override val providerName: String = "RoundRobin"

    private val currentIndex = AtomicInteger(0)

    fun getNextFactory(): AdFactory {
        if (factories.isEmpty()) throw IllegalStateException("No AdFactories provided!")
        val index = (currentIndex.getAndIncrement() and Int.MAX_VALUE) % factories.size
        return factories[index]
    }

    override fun createInterstitialAdController(): InterstitialAdController {
        return getNextFactory().createInterstitialAdController()
    }

    override fun createRewardedAdController(): RewardedAdController {
        return getNextFactory().createRewardedAdController()
    }

    override fun createAppOpenAdController(): AppOpenAdController {
        return getNextFactory().createAppOpenAdController()
    }
}
