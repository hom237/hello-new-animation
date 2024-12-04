package com.daily.new_amime.for_my.main.ui.wiget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.util.Log
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import com.daily.new_amime.for_my.networking.anime.AnimeRepository
import com.daily.new_amime.for_my.networking.daily_anime.DailyDto
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

@AndroidEntryPoint
class NewAnimeWidgetReceiver : GlanceAppWidgetReceiver() {
    @Inject
    lateinit var animeRepository: AnimeRepository

    @OptIn(DelicateCoroutinesApi::class)
    suspend fun getDailySuspendAnime(): List<DailyDto> {
        return GlobalScope.async(Dispatchers.Main) {
            kotlin.runCatching {
                animeRepository.getAnimationDaily().first() // Flow의 첫 번째 결과만 가져오기
            }.getOrElse { e ->
                Log.e("getDailyAnime", "Error fetching daily anime", e)
                emptyList()
            }
        }.await()
    }


    override val glanceAppWidget: GlanceAppWidget =
        NewAnimeWidget(){
            GlobalScope.async(Dispatchers.Main) {
                kotlin.runCatching {
                    animeRepository.getAnimationDaily()
                }.getOrElse { e ->
                    Log.e("getDailyAnime", "Error fetching daily anime", e)
                    flow<List<DailyDto>> {
                        emit(emptyList())
                    }
                }
            }.await()
        }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }
}