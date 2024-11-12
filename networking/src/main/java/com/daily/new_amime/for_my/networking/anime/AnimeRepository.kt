package com.daily.new_amime.for_my.networking.anime

import com.daily.new_amime.for_my.networking.daily_anime.DailyDto
import kotlinx.coroutines.flow.Flow

interface AnimeRepository {
    suspend fun getAnimationDaily() : Flow<List<DailyDto>>
}