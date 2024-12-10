package com.daily.new_amime.for_my.main.di

import com.daily.new_amime.for_my.networking.anime.AnimeRepository
import com.daily.new_amime.for_my.networking.anime.AnimeRepositoryImpl
import com.daily.new_amime.for_my.networking.image.ImageRepository
import com.daily.new_amime.for_my.networking.image.ImageRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class RepositoryModule {
    @Provides
    @Singleton
//    @RetrofitModule.SubDomainRetrofit
    fun subProvideAnimeRepository(animeRepositoryImpl: ImageRepositoryImpl): ImageRepository =
        animeRepositoryImpl

    @Provides
    @Singleton
//    @RetrofitModule.MainDomainRetrofit
    fun provideAnimeRepository(animeRepositoryImpl: AnimeRepositoryImpl): AnimeRepository =
        animeRepositoryImpl
}