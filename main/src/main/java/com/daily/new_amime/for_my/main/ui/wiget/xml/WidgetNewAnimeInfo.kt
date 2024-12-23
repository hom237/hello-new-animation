package com.daily.new_amime.for_my.main.ui.wiget.xml

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.util.Log
import android.widget.RemoteViews
import androidx.lifecycle.asLiveData
import com.daily.new_amime.for_my.main.R
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
import javax.inject.Inject

@AndroidEntryPoint
class WidgetNewAnimeInfo : AppWidgetProvider() {
    var testFlow = MutableStateFlow(emptyList<DailyDto>())
    var testImageFlow = MutableStateFlow<File>(File(""))

    @Inject
    lateinit var animeRepository: AnimeRepository

    @Inject
    lateinit var imageRepository: ImageRepository

    companion object {
        var page = 0
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        Log.d("WidgetLifeCycle", "onUpdate")
        getAnimeData()
        testImageFlow.onEach() {
            for (appWidgetId in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId)
            }
        }.launchIn(GlobalScope)
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
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

    private fun getAnimeData() {
        runBlocking {
            kotlin.runCatching {
                animeRepository.getAnimationDaily()
                    .collect() {
                        testFlow.emit(it)
                    }
            }.getOrElse { e ->
                testFlow.emit(emptyList())
                flow<List<DailyDto>> {
                    emit(emptyList())
                }
            }
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        val widgetAction = intent?.action
        Log.d("WidgetLifeCycle", "onReceive action: $widgetAction")
        if (context != null) {
            when (widgetAction) {
                "com.example.ACTION_NEXT_BUTTON" -> {
                    ++page
                }

                "com.example.ACTION_BEFORE_BUTTON" -> {
                    if (page > 0) --page
                }
            }
            Log.d("xml", "title : ${context.cacheDir.list()?.get(page)}")
            Log.d("xml", "page : $page")
            val appWidgetManager = AppWidgetManager.getInstance(context)
            if (intent != null) {
                val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 0)
                Log.d("code", "id : ${appWidgetId}")
                updateAppWidget(
                    context,
                    appWidgetManager,
                    appWidgetId
                )
            }
        }
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

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
    ) {
        Log.d("WidgetLifeCycle", "updateAppWidget")
        val nextIntent = Intent(context, WidgetNewAnimeInfo::class.java).apply {
            action = "com.example.ACTION_NEXT_BUTTON"
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }

        val beforeIntent = Intent(context, WidgetNewAnimeInfo::class.java).apply {
            action = "com.example.BEFORE_BUTTON_CLICK"
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
        val test = testFlow.asLiveData()

        val widgetText = context.getString(R.string.appwidget_text)
        Log.d("test", "id : $appWidgetId")

        val views = RemoteViews(context.packageName, R.layout.widget_new_anime_info)
            .apply {
                Log.d("xml", "file : ${context.cacheDir.list()?.get(page)}")
                Log.d(
                    "xml",
                    "absolutePath : ${context.cacheDir.absolutePath}/${
                        context.cacheDir.list()?.get(page)
                    }"
                )
                Log.d(
                    "xml",
                    "path : ${context.cacheDir.path}/${context.cacheDir.list()?.get(page)}"
                )
//              setImageViewUri(R.id.animeImage, context.cacheDir.absolutePath.toUri())
                setTextViewText(R.id.animeName, "${page}번째")
                setOnClickPendingIntent(R.id.nextButton, nextPendingIntent)
                setOnClickPendingIntent(R.id.beforeButton, beforePendingIntent)

                setImageViewBitmap(
                    R.id.animeImage,
                    BitmapFactory.decodeFile(context.cacheDir.listFiles()?.get(page)?.absolutePath)
                )
                setImageViewBitmap(
                    R.id.animeImage,
                    BitmapFactory.decodeFile(context.cacheDir.listFiles()?.get(page+2)?.absolutePath)
                )
//              setImageViewUri(R.id.animeImage, Uri.parse("https://blog.kakaocdn.net/dn/byU2np/btqBQ1PPp3j/H4CQv7CftyO3rlI5kgmIVk/img.jpg"))
            }
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}
