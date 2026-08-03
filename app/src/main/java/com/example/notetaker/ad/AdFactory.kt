package com.example.notetaker.ad

enum class AdType {
    INTERSTITIAL,
    REWARDED,
    APP_OPEN
}

abstract class AdFactory {
    abstract fun createAdController(type: AdType): AdController

    fun preloadAd(type: AdType, context: android.content.Context): AdController {
        val controller = createAdController(type)
        controller.loadAd(context)
        return controller
    }
}

class DefaultAdFactory : AdFactory() {
    override fun createAdController(type: AdType): AdController {
        return when (type) {
            AdType.INTERSTITIAL -> InterstitialAdController()
            AdType.REWARDED -> RewardedAdController()
            AdType.APP_OPEN -> AppOpenAdController()
        }
    }
}
