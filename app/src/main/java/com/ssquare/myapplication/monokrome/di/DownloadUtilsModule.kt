package com.ssquare.myapplication.monokrome.di

import android.content.Context
import com.ssquare.myapplication.monokrome.data.Repository
import com.ssquare.myapplication.monokrome.util.DownloadUtils
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
class DownloadUtilsModule {

    @Singleton
    @Provides
    fun provideDownloadUtils(
        @ApplicationContext context: Context,
        repository: Repository
    ): DownloadUtils = DownloadUtils(context, repository)

}