package com.daily.new_amime.for_my.networking.anime

import com.daily.new_amime.for_my.network.dto.daily_anime.DailyDto
import retrofit2.http.GET

interface AnimeApi {
    @GET("/api/search/v2/daily/")
    fun getAnimationDaily() : List<DailyDto>
}