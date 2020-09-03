package com.ssquare.myapplication.monokrome.di

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.ssquare.myapplication.monokrome.network.BASE_URL
import com.ssquare.myapplication.monokrome.network.MonokromeApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton


@Module
@InstallIn(ApplicationComponent::class)
class NetworkModule {

    @Singleton
    @Provides
    fun provideClient(): OkHttpClient = OkHttpClient.Builder()
        .readTimeout(30, TimeUnit.SECONDS)
        .connectTimeout(30, TimeUnit.SECONDS)
        .build()

    @Singleton
    @Provides
    fun provideMoshi(): Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()


    @Singleton
    @Provides
    fun provideRetrofit(moshi: Moshi, client: OkHttpClient): Retrofit = Retrofit.Builder()
        .addConverterFactory(MoshiConverterFactory.create(moshi).asLenient())
        .client(client)
        .baseUrl(BASE_URL)
        .build()


    @Singleton
    @Provides
    fun provideMonokromeApiService(retrofit: Retrofit): MonokromeApiService =
        retrofit.create(MonokromeApiService::class.java)

}
