package com.daily.new_amime.for_my.networking.image

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Url

interface ImageApi {
    @GET
    fun getAnimationImage(
        @Url url: String
    ): Call<ResponseBody>
}