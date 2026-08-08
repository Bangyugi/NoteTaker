package com.example.notetaker.di

import android.content.Context
import com.example.notetaker.data.repository.BillingRepository
import com.example.notetaker.data.repository.BillingRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BillingModule {

    @Provides
    @Singleton
    fun provideBillingRepository(
        @ApplicationContext context: Context
    ): BillingRepository {
        return BillingRepositoryImpl(context)
    }
}
