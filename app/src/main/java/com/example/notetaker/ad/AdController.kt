package com.example.notetaker.ad

import android.app.Activity
import android.content.Context

interface AdController {
    fun loadAd(context: Context)
    fun showAd(activity: Activity, onComplete: () -> Unit = {})
    fun isReady(context: Context): Boolean
}
