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
import java.util.Calendar
import javax.inject.Inject

@AndroidEntryPoint
class WidgetAnimeInfoVertical : AppWidgetProvider() {
    var testFlow = MutableStateFlow(emptyList<DailyDto>())
    var testImageFlow = MutableStateFlow<File>(File(""))

    @Inject
    lateinit var animeRepository: AnimeRepository

    @Inject
    lateinit var imageRepository: ImageRepository

    companion object {
        var page = 0
        val date = listOf("일요일", "월요일", "화요일", "수요일", "목요일", "금요일", "토요일")
        var animeList :List<DailyDto> = emptyList<DailyDto>()
        var today = date[0]
        val imageFileList = mutableMapOf<String, File>()
        val smallView = R.layout.widget_new_anime_horizon_small
        val largeView = R.layout.widget_new_anime_horizon_large

    }

    override fun onAppWidgetOptionsChanged(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetId: Int,
        newOptions: Bundle?
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        // Get the new sizes.
        val sizes = newOptions?.getParcelableArrayList<SizeF>(
            AppWidgetManager.OPTION_APPWIDGET_SIZES
        )
        Log.d("test", "size : ${sizes}")
        // Check that the list of sizes is provided by the launcher.
        if (sizes.isNullOrEmpty()) {
            return
        }
        val largeViews = RemoteViews(context?.packageName, largeView)
            .apply {
//              setImageViewUri(R.id.animeImage, context.cacheDir.absolutePath.toUri())
                if (animeList.isNotEmpty()) {
                    val animeData = animeList[page]
                    Log.d("test", "anime : $animeData")
                    setTextViewText(R.id.animeName, animeData.name)
                    setTextViewText(R.id.ratingOfAnime, animeData.content_rating)
                }
//              setImageViewUri(R.id.animeImage, Uri.parse("https://blog.kakaocdn.net/dn/byU2np/btqBQ1PPp3j/H4CQv7CftyO3rlI5kgmIVk/img.jpg"))
            }

        val smallViews = RemoteViews(context?.packageName, smallView)
            .apply {
                if (animeList.isNotEmpty()) {
                    val animeData = animeList[page]
                    Log.d("test", "anime : $animeData")
                    setTextViewText(R.id.animeName, animeData.name)
                }
            }
        val viewMapping: Map<SizeF, RemoteViews> = mapOf(
            SizeF(455f, 110f) to smallViews,
            SizeF(57f, 337f) to largeViews
        )

        // Map the sizes to the RemoteViews that you want.
//        val remoteViews = RemoteViews(sizes.associateWith(::createRemoteViews))
        appWidgetManager?.updateAppWidget(appWidgetId,smallViews)
    }


    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        Log.d("WidgetLifeCycle", "onUpdate")
        val calendar: Calendar = Calendar.getInstance()
        val todayOfWeek: Int = calendar.get(Calendar.DAY_OF_WEEK)
        today = date[todayOfWeek-1]
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
        // Enter relevant functionality for when the first widget is created
        super.onEnabled(context)
        Log.d("WidgetLifeCycle", "onEnabled")
    }

    override fun onRestored(context: Context?, oldWidgetIds: IntArray?, newWidgetIds: IntArray?) {
        super.onRestored(context, oldWidgetIds, newWidgetIds)
        Log.d("WidgetLifeCycle", "onEnabled")
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        Log.d("WidgetLifeCycle", "onDisabled")
        // Enter relevant functionality for when the last widget is disabled
    }

    private fun getAnimeData(context : Context) {
        Log.d("test", "in fun 1")
        runBlocking {
            kotlin.runCatching {
                animeRepository.getAnimationDaily()
                    .collect() {
                        animeList = it.filter { it.distributed_air_time ==  today}
                        Log.d("test", "page : ${animeList.size}")

                        animeList.forEachIndexed() { position, animeData ->
                            var name = animeData.name
                            Log.d("test", "unEncode : $name")
                            if (name.contains("/")){
                                Log.d("test", "in if")
                                name = name.replace("/", "_")
                                Log.d("test", "name : $name")
                            }
                            Log.d("test", "name : $name")
                            imageFileList[name]= File(
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
                    Log.d("test", "in fun 2-2")
                    emit(emptyList())
                }
            }
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        val widgetAction = intent?.action
        Log.d("test", "day : $today")
        if (context != null) {
            val imageList = context.cacheDir.listFiles()?.filter { it.name.contains(WidgetRegex.isJpg.toRegex()) }?: emptyList()
            when (widgetAction) {
                "com.example.ACTION_NEXT_BUTTON" -> {
                    if ((imageList.size-1) > page) {
                        ++page
                    }
                }
                "com.example.ACTION_BEFORE_BUTTON" -> {

                    if (page > 0) {
                        --page
                    }
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


    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
    ) {

        Log.d("WidgetLifeCycle", "updateAppWidget")
        val nextIntent = Intent(context, WidgetAnimeInfoVertical::class.java).apply {
            action = "com.example.ACTION_NEXT_BUTTON"
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }

        val beforeIntent = Intent(context, WidgetAnimeInfoVertical::class.java).apply {
            action = "com.example.ACTION_BEFORE_BUTTON"
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

        val imageList = context.cacheDir.listFiles().filter { it.name.contains(WidgetRegex.isJpg.toRegex()) }

        val largeViews = RemoteViews(context.packageName, largeView)
            .apply {
//              setImageViewUri(R.id.animeImage, context.cacheDir.absolutePath.toUri())
                if (animeList.isNotEmpty()) {
                    val animeData = animeList[page]
                    Log.d("test", "anime : $animeData")
                    setTextViewText(R.id.animeName, animeData.name)
                    setTextViewText(R.id.ratingOfAnime, animeData.content_rating)
                }
                setOnClickPendingIntent(R.id.nextButton, nextPendingIntent)
                setOnClickPendingIntent(R.id.beforeButton, beforePendingIntent)

                    if (imageList.isNotEmpty()) {
                        setImageViewBitmap(
                            R.id.animeImage,
                            BitmapFactory.decodeFile(imageList.get(page)?.absolutePath)
                        )
                    }
//              setImageViewUri(R.id.animeImage, Uri.parse("https://blog.kakaocdn.net/dn/byU2np/btqBQ1PPp3j/H4CQv7CftyO3rlI5kgmIVk/img.jpg"))
            }

        val smallViews = RemoteViews(context.packageName, smallView)
            .apply {
                if (animeList.isNotEmpty()) {
                    if  (animeList.size > page) {
                        val animeData = animeList[page]
                        Log.d("test", "anime : $animeData")
                        setTextViewText(R.id.animeName, animeData.name)
                    }else{
                        setTextViewText(R.id.animeName, "값 없음")
                    }
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

    private fun getAnimeImage(url: String, file: File) {
        Log.d("test", "in fun")
        runBlocking {
            kotlin.runCatching {
                Log.d("test", "path : $url")
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
