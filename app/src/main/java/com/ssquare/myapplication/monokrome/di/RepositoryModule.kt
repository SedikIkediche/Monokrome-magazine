package com.ssquare.myapplication.monokrome.di

import android.content.Context
import com.ssquare.myapplication.monokrome.data.AuthRepository
import com.ssquare.myapplication.monokrome.data.Repository
import com.ssquare.myapplication.monokrome.db.LocalCache
import com.ssquare.myapplication.monokrome.network.MonokromeApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton


@Module
@InstallIn(ApplicationComponent::class)
class RepositoryModule {

    @Singleton
    @Provides
    fun provideCoroutineScope(): CoroutineScope = CoroutineScope(Dispatchers.Main)


    @Singleton
    @Provides
    fun provideRepository(
        @ApplicationContext context: Context,
        scope: CoroutineScope,
        cache: LocalCache,
        network: MonokromeApiService
    ): Repository = Repository(context, scope, cache, network)

    @Singleton
    @Provides
    fun provideAuthRepository(
        @ApplicationContext context: Context,
        network: MonokromeApiService
    ): AuthRepository = AuthRepository(context, network)


}