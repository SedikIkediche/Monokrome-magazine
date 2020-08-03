package com.ssquare.myapplication.monokrome.di

import android.content.Context
import androidx.room.Room
import com.ssquare.myapplication.monokrome.db.MagazineDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
class DatabaseModule {

    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext context: Context) = Room.databaseBuilder(
        context.applicationContext,
        MagazineDatabase::class.java,
        MagazineDatabase.DATABASE_NAME
    ).build()

    @Singleton
    @Provides
    fun provideMagazineDao(magazineDatabase: MagazineDatabase) = magazineDatabase.magazineDao

    @Singleton
    @Provides
    fun provideHeaderDao(magazineDatabase: MagazineDatabase) = magazineDatabase.headerDao
}