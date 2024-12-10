package com.daily.new_amime.for_my.networking.anime

import com.daily.new_amime.for_my.annotation.RetrofitModule
import com.daily.new_amime.for_my.networking.daily_anime.DailyDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class AnimeRepositoryImpl @Inject constructor(
    @RetrofitModule.MainDomainRetrofit
    private val mainAnimeApi: AnimeApi,
) : AnimeRepository {
    override fun getAnimationDaily(): Flow<List<DailyDto>> = flow {
        emit(mainAnimeApi.getAnimationDaily())
    }
}