package com.daily.new_amime.for_my.main.di

import com.daily.new_amime.for_my.annotation.RetrofitModule
import com.daily.new_amime.for_my.main.ignore.URL
import com.daily.new_amime.for_my.networking.anime.AnimeApi
import com.daily.new_amime.for_my.networking.image.ImageApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {
    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }

    @Provides
    @Singleton
    @RetrofitModule.MainDomainRetrofit
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
    }

    @Provides
    @Singleton
    @RetrofitModule.SubDomainRetrofit
    fun subProvideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://thumbnail.laftel.net")
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
    }

    @Provides
    @Singleton
    @RetrofitModule.MainDomainRetrofit
    fun provideAnimeApi(@RetrofitModule.MainDomainRetrofit retrofit: Retrofit): AnimeApi =
        retrofit.create(AnimeApi::class.java)

    @Provides
    @Singleton
    @RetrofitModule.SubDomainRetrofit
    fun subProvideAnimeApi(@RetrofitModule.SubDomainRetrofit retrofit: Retrofit): ImageApi =
        retrofit.create(ImageApi::class.java)

    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggerInterceptor: HttpLoggingInterceptor,
    ): OkHttpClient {
        return OkHttpClient().newBuilder().apply {
            connectTimeout(60, TimeUnit.SECONDS)
            readTimeout(60, TimeUnit.SECONDS)
            writeTimeout(60, TimeUnit.SECONDS)
            addInterceptor(loggerInterceptor)
        }.build()
    }
}