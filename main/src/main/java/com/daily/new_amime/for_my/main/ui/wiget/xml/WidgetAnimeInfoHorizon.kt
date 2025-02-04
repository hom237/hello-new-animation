package com.daily.new_amime.for_my.main.ui.wiget.xml

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.util.SizeF
import android.widget.RemoteViews
import com.daily.new_amime.for_my.main.R
import com.daily.new_amime.for_my.main.unit.WidgetRegex
import com.daily.new_amime.for_my.networking.anime.AnimeRepository
import com.daily.new_amime.for_my.networking.daily_anime.DailyDto
import com.daily.new_amime.for_my.networking.image.ImageRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking
import java.io.File
import java.time.LocalDate
import java.util.Calendar
import javax.inject.Inject

@AndroidEntryPoint
class WidgetAnimeInfoHorizon : AppWidgetProvider() {
    var testFlow = MutableStateFlow(emptyList<DailyDto>())
    var testImageFlow = MutableStateFlow<File>(File(""))

    @Inject
    lateinit var animeRepository: AnimeRepository

    @Inject
    lateinit var imageRepository: ImageRepository

    companion object {
        var page = 0
        val date = listOf("일요일", "월요일", "화요일", "수요일", "목요일", "금요일", "토요일")
        var animeList: List<DailyDto> = emptyList<DailyDto>()
        var today = date[0]
        val imageFileList = mutableMapOf<String, File>()
        val smallView = R.layout.widget_new_anime_horizon_small
        val largeView = R.layout.widget_new_anime_horizon_large

    }

    override fun onAppWidgetOptionsChanged(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetId: Int,
        newOptions: Bundle?,
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        val sizes = newOptions?.getParcelableArrayList<SizeF>(
            AppWidgetManager.OPTION_APPWIDGET_SIZES
        )
        if (sizes.isNullOrEmpty()) {
            return
        }
        val largeViews = RemoteViews(context?.packageName, largeView)
            .apply {
                if (animeList.isNotEmpty()) {
                    val animeData = animeList[page]
                    setTextViewText(R.id.animeName, today)
                    setTextViewText(R.id.ratingOfAnime, animeData.content_rating)
                    setTextViewText(R.id.ratingOfAnime, animeData.content_rating)
                }
            }

        val smallViews = RemoteViews(context?.packageName, smallView)
            .apply {
                if (animeList.isNotEmpty()) {
                    val animeData = animeList[page]
                    setTextViewText(R.id.animeName, animeData.name)
                    setTextViewText(
                        R.id.updateOfDay,
                        getAnimeUpdateData(animeData.latest_episode_created?.substring(0, 10))
                    )
                }
            }
        val viewMapping: Map<SizeF, RemoteViews> = mapOf(
            SizeF(455f, 110f) to largeViews,
            SizeF(57f, 337f) to smallViews
        )

        appWidgetManager?.updateAppWidget(appWidgetId, RemoteViews(viewMapping))
    }


    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        val calendar: Calendar = Calendar.getInstance()
        val todayOfWeek: Int = calendar.get(Calendar.DAY_OF_WEEK)
        today = date[todayOfWeek - 1]
        deleteFile(context)
        getAnimeData(context = context)
        testFlow.onEach {
            for (appWidgetId in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId)
            }
        }.launchIn(GlobalScope)
    }

    private fun deleteFile(context: Context) {
        context.cacheDir.listFiles()?.forEach {
            it.delete()
        }
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
    }

    override fun onRestored(context: Context?, oldWidgetIds: IntArray?, newWidgetIds: IntArray?) {
        super.onRestored(context, oldWidgetIds, newWidgetIds)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
    }

    private fun getAnimeData(context: Context) {
        runBlocking {
            kotlin.runCatching {
                animeRepository.getAnimationDaily()
                    .collect() {
                        animeList = it.filter { it.distributed_air_time == today }
                        Log.d("test", "page : ${animeList.size}")
                        animeList.forEach {
                            Log.d("test", "name : ${it.name}")
                        }

                        animeList.forEachIndexed() { position, animeData ->
                            var name = animeData.name
                            if (name.contains("/")) {
                                name = name.replace("/", "_")
                            }
                            imageFileList[name] = File(
                                context.cacheDir,
                                "${name.replace(" ", "_")}.jpg"
                            )

                            imageFileList[name]?.let { it1 -> getAnimeImage(animeData.img, it1) }
                        }

                        testFlow.emit(it)
                    }
            }.getOrElse { e ->
                testFlow.emit(emptyList())
                flow<List<DailyDto>> {
                    animeList = emptyList()
                    emit(emptyList())
                }
            }
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        val widgetAction = intent?.action
        if (context != null) {
            val imageList = context.cacheDir.listFiles()
                ?.filter { it.name.contains(WidgetRegex.isJpg.toRegex()) } ?: emptyList()
            when (widgetAction) {
                "com.example.ACTION_NEXT_BUTTON" -> {
                    Log.d("test", "clicked next button")
                    if ((imageList.size - 1) > page) {
                        ++page
                    }
                }
                "com.example.ACTION_BEFORE_BUTTON" -> {
                    Log.d("test", "clicked before button")
                    if (page > 0) {
                        --page
                    }
                }
                "com.example.ACTION_REFRESH" -> {
                    Log.d("test", "clicked refresh button")
                    initWidget(context)
                }
            }
            val appWidgetManager = AppWidgetManager.getInstance(context)
            if (intent != null) {
                val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 0)
                updateAppWidget(
                    context,
                    appWidgetManager,
                    appWidgetId
                )
            }
        }
    }

    private fun initWidget(context: Context){
        val calendar: Calendar = Calendar.getInstance()
        val todayOfWeek: Int = calendar.get(Calendar.DAY_OF_WEEK)
        WidgetAnimeInfoVertical.today = WidgetAnimeInfoVertical.date[todayOfWeek-1]
        deleteFile(context)
        getAnimeData(context = context)
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
    ) {
        val nextIntent = Intent(context, WidgetAnimeInfoHorizon::class.java).apply {
            action = "com.example.ACTION_NEXT_BUTTON"
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }
        val beforeIntent = Intent(context, WidgetAnimeInfoHorizon::class.java).apply {
            action = "com.example.ACTION_BEFORE_BUTTON"
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }
        val refreshIntent = Intent(context, WidgetAnimeInfoVertical::class.java).apply {
            action = "com.example.ACTION_REFRESH"
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }

        val beforePendingIntent = PendingIntent.getBroadcast(
            context,
            1,
            beforeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val nextPendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            nextIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val refreshPendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            refreshIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )


        val imageList =
            context.cacheDir.listFiles().filter { it.name.contains(WidgetRegex.isJpg.toRegex()) }

        val largeViews = RemoteViews(context.packageName, largeView)
            .apply {
                if (animeList.isNotEmpty()) {
                    val animeData = animeList[page]
                    setTextViewText(R.id.animeName, animeData.name)
                    setTextViewText(R.id.dayOfAnime, "${today.substring(0, 2)}애니")
                    setTextViewText(R.id.ratingOfAnime, animeData.content_rating)
                    setTextViewText(
                        R.id.updateOfDay,
                        getAnimeUpdateData(animeData.latest_episode_created?.substring(0, 10))
                    )
                }
                setOnClickPendingIntent(R.id.nextButton, nextPendingIntent)
                setOnClickPendingIntent(R.id.beforeButton, beforePendingIntent)
                setOnClickPendingIntent(R.id.refresh, refreshPendingIntent)


                if (imageList.isNotEmpty()) {
                    setImageViewBitmap(
                        R.id.animeImage,
                        BitmapFactory.decodeFile(imageList.get(page)?.absolutePath)
                    )
                }
            }

        val smallViews = RemoteViews(context.packageName, smallView)
            .apply {
                if (animeList.isNotEmpty()) {
                    val animeData = animeList[page]
                    setTextViewText(R.id.animeName, animeData.name)
                    setTextViewText(
                        R.id.updateOfDay,
                        getAnimeUpdateData(animeData.latest_episode_created?.substring(0, 10))
                    )
                }
                setOnClickPendingIntent(R.id.nextButton, nextPendingIntent)
                setOnClickPendingIntent(R.id.beforeButton, beforePendingIntent)
            }
        val viewMapping: Map<SizeF, RemoteViews> = mapOf(
            SizeF(349f, 102f) to smallViews,
            SizeF(349f, 220f) to largeViews
        )
        appWidgetManager.updateAppWidget(appWidgetId, RemoteViews(viewMapping))
    }

    private fun getAnimeUpdateData(date: String?): String {
        val result = ""
        if (date.isNullOrEmpty()) {
            return result + "공개 예정"
        } else {
            val latestDate = LocalDate.parse(date.substring(0, 10))
            return "${latestDate.year}년 ${latestDate.monthValue}월 ${latestDate.dayOfMonth}일"
        }
    }

    private fun getAnimeImage(url: String, file: File) {
        runBlocking {
            kotlin.runCatching {
                imageRepository.getAnimationImage(
                    url = url,
                    file
                ).collect() {
                    Log.d("test", "result : ${it.contentType()}")
                }

            }.getOrElse { e ->
                e.printStackTrace()
                testImageFlow.emit(File(""))
                flow<List<DailyDto>> {
                    emit(emptyList())
                }
            }
        }
    }
}
