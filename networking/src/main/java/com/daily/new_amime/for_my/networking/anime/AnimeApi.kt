package com.daily.new_amime.for_my.networking.anime

import com.daily.new_amime.for_my.networking.daily_anime.DailyDto
//import com.daily.new_amime.for_my.networking.daily_anime.Image
import retrofit2.http.GET

interface AnimeApi {
    @GET("/api/search/v2/daily/")
    suspend fun getAnimationDaily(): List<DailyDto>
}