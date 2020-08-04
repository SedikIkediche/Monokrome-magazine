package com.ssquare.myapplication.monokrome.di

import android.content.Context
import com.ssquare.myapplication.monokrome.util.networkcheck.ConnectivityProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton


@Module
@InstallIn(ApplicationComponent::class)
class ConnectivityModule {

    @Singleton
    @Provides
    fun provideConnectivityProvider(@ApplicationContext context: Context): ConnectivityProvider =
        ConnectivityProvider.createProvider(context)

}