package com.example.notetaker

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.notetaker.ad.AdFactory
import com.example.notetaker.ad.AdType
import com.example.notetaker.ad.AppOpenAdManager
import com.example.notetaker.ad.DefaultAdFactory
import com.example.notetaker.ad.NativeAdManager
import com.google.android.libraries.ads.mobile.sdk.MobileAds
import com.google.android.libraries.ads.mobile.sdk.initialization.InitializationConfig
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@HiltAndroidApp
class NoteTakerApplication: Application(), Application.ActivityLifecycleCallbacks,
DefaultLifecycleObserver{

    private lateinit var appOpenAdManager: AppOpenAdManager
    private var currentActivity: Activity? = null

    override fun onCreate() {
        super<Application>.onCreate()
        registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        appOpenAdManager = AppOpenAdManager()
        val adFactory: AdFactory = DefaultAdFactory()

        CoroutineScope(Dispatchers.IO).launch {
            MobileAds.initialize(this@NoteTakerApplication, InitializationConfig.Builder(APP_ID).build()) {
                adFactory.preloadAd(AdType.APP_OPEN, this@NoteTakerApplication)
                adFactory.preloadAd(AdType.INTERSTITIAL, this@NoteTakerApplication)
                NativeAdManager.loadAd(this@NoteTakerApplication, "home_native")
            }
        }
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        currentActivity?.let { activity ->
            appOpenAdManager.showAdIfAvailable(activity) {

            }
        }
    }
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityStarted(activity: Activity) {
        if (!appOpenAdManager.isShowingAd) {
            currentActivity = activity
        }
    }

    override fun onActivityResumed(activity: Activity) {
        if (!appOpenAdManager.isShowingAd) {
            currentActivity = activity
        }
    }

    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {
        if (currentActivity == activity) {
            currentActivity = null
        }
    }
    private companion object {
        const val APP_ID = "ca-app-pub-3940256099942544~3347511713"
    }

}