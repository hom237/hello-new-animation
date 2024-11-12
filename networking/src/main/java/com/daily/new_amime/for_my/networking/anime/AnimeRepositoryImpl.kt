package com.daily.new_amime.for_my.networking.anime

import com.daily.new_amime.for_my.networking.daily_anime.DailyDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class AnimeRepositoryImpl @Inject constructor(
    private val animeApi: AnimeApi
) : AnimeRepository {
    override suspend fun getAnimationDaily(): Flow<List<DailyDto>> = flow {
        emit(animeApi.getAnimationDaily())
    }
}