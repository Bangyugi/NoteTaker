package com.example.notetaker.data.repository

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

interface BillingRepository {
    val isAdFree: StateFlow<Boolean>
    fun buyRemoveAds()
    fun resetPurchase()
    fun setAdFreeState(isAdFree: Boolean)
}

@Singleton
class BillingRepositoryImpl @Inject constructor(
    context: Context
) : BillingRepository {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    private val _isAdFree = MutableStateFlow(prefs.getBoolean(KEY_IS_AD_FREE, false))
    override val isAdFree: StateFlow<Boolean> = _isAdFree.asStateFlow()

    override fun buyRemoveAds() {
        setAdFreeState(true)
    }

    override fun resetPurchase() {
        setAdFreeState(false)
    }

    override fun setAdFreeState(isAdFree: Boolean) {
        prefs.edit().putBoolean(KEY_IS_AD_FREE, isAdFree).apply()
        _isAdFree.value = isAdFree
    }

    companion object {
        private const val PREF_NAME = "billing_preferences"
        private const val KEY_IS_AD_FREE = "key_is_ad_free"
    }
}
