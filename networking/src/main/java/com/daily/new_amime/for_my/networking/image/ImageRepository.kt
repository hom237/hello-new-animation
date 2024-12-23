package com.daily.new_amime.for_my.networking.image

import kotlinx.coroutines.flow.Flow
import okhttp3.ResponseBody
import java.io.File

interface ImageRepository {
    fun getAnimationImage(url : String, file: File): Flow<ResponseBody>
}