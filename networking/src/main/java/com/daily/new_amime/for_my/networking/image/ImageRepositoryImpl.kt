package com.daily.new_amime.for_my.networking.image

import android.util.Log
import com.daily.new_amime.for_my.annotation.RetrofitModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.ResponseBody
import retrofit2.Response
import java.io.File
import javax.inject.Inject

class ImageRepositoryImpl @Inject constructor(
    @RetrofitModule.SubDomainRetrofit
    private val subAnimeApi: ImageApi,
) : ImageRepository {
    override fun getAnimationImage(url: String, file: File): Flow<ResponseBody> = flow {
        Log.d("test", "in get")
        val response: Response<ResponseBody> = subAnimeApi.getAnimationImage(url).execute()
        if (response.isSuccessful && response.body() != null) {
            Log.d("test", "body : ${response.body()}")
            if (response.body() != null) {
                saveImage(response.body()!!, file)
            }
            emit(response.body()!!)
        } else {
            throw Exception("Failed to download image. Code: ${response.code()}")
        }
    }.flowOn(Dispatchers.Default).catch { e ->
        e.printStackTrace()
        throw Exception("Failed to download image. Code: ${e}")
    }

    private fun saveImage(responseBody: ResponseBody, destinationFile: File) {
        responseBody.use { body ->
            Log.d("test", "body : $body")
            destinationFile.outputStream().use { outputStream ->
                body.byteStream().copyTo(outputStream)
            }
        }
    }
}