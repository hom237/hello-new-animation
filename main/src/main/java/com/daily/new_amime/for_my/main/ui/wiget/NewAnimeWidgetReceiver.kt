package com.daily.new_amime.for_my.main.ui.wiget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.glance.Button
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.Text
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.daily.new_amime.for_my.networking.anime.AnimeRepository
import com.daily.new_amime.for_my.networking.daily_anime.DailyDto
import com.daily.new_amime.for_my.networking.image.ImageRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import java.io.File
import javax.inject.Inject

@Suppress("UNREACHABLE_CODE")
@AndroidEntryPoint
class NewAnimeWidgetReceiver : GlanceAppWidgetReceiver() {
    @Inject
    lateinit var animeRepository: AnimeRepository

    @Inject
    lateinit var imageRepository: ImageRepository

    override val glanceAppWidget: GlanceAppWidget =
        NewInnerAnimeWidget()

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }

    inner class NewInnerAnimeWidget() : GlanceAppWidget() {
        var testFlow = MutableStateFlow(emptyList<DailyDto>())
        var testImageFlow = MutableStateFlow<File>(File(""))

        override suspend fun provideGlance(context: Context, id: GlanceId) {
            getAnimeData()
            provideContent {

                // create your AppWidget here
                GlanceTheme {
                    MyContent()
                }
            }
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

        private fun getAnimeImage(path: String) {
            Log.d("test", "in fun")
            runBlocking {
                kotlin.runCatching {
                    val customPath = path.replace(
                        "https://thumbnail.laftel.net/",
                        ""
                    )
                    Log.d("test", "path : $customPath")
                    imageRepository.getAnimationImage(
                        path = customPath
                    )
                        .collect() {
                            testImageFlow.emit(it)
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


        @OptIn(ExperimentalGlideComposeApi::class)
        @Composable
        private fun MyContent() {
            val test by testFlow.collectAsState(emptyList())

            LaunchedEffect(test) {
                if (test.isNotEmpty()) {
                    getAnimeImage(test[0].img)
                }
            }

            Log.d("size", "widget size : $sizeMode")
            val cont = remember {
                mutableIntStateOf(0)
            }
            Column(
                modifier = GlanceModifier.fillMaxSize()
                    .background(GlanceTheme.colors.background),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalAlignment = Alignment.Vertical.CenterVertically
            ) {


                val context = LocalContext.current
//            val imageBitmap = produceState<Bitmap?>(initialValue = null) {
//                value = downloadImage(
//                    "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSJqKFIq8SX_H7NZTEwC5SA-JnSxBD87FLziA&s"
//                )
//            }

//            GlanceModifier.fillMaxSize().let { modifier ->
//                Column(modifier = modifier.padding(16.dp)) {
//                    if (imageBitmap.value != null) {
//                        Image(
//                            provider = ImageProvider(imageBitmap.value!!),
//                            contentDescription = "Internet Image",
//                            modifier = GlanceModifier.fillMaxWidth().height(150.dp)
//                        )
//                    } else {
//                        Text("Loading image...")
//                    }
//                }
//            }

//                AsyncImage(
//                    model = if(test.isNotEmpty()) {
//                        Log.d("Test", "imag : ${test[0].img}")
//                        test[0].img
//                    }else{
//                        ""
//                    },
//                    contentDescription = "",
//                )

                Text(
                    text = "${test.size} : ${cont.intValue}",
                    modifier = GlanceModifier.padding(12.dp)
                )
                Row(horizontalAlignment = Alignment.CenterHorizontally) {
                    Button(
                        text = "+",
                        onClick = {
                            cont.value = ++cont.value
                        }
                    )
                    Button(
                        text = "-",
                        onClick = {
                            cont.value = --cont.value
                        }
                    )
                }
            }
        }


//    @OptIn(DelicateCoroutinesApi::class)
//    private fun downloadImage(imageUrl: String): Bitmap? {
//
//        return try {
//            var input : InputStream? = null
//            GlobalScope.launch {
//                val url = URL(imageUrl)
//                val connection = url.openConnection() as HttpURLConnection
//                connection.doInput = true
//                connection.connect()
//                input = connection.inputStream
//                Log.d("test", "input : $input")
//                return@launch
//            }
//            val bitmap = BitmapFactory.decodeStream(input)
//            Log.d("bitmap", "map : $bitmap")
//            bitmap
//        } catch (e: Exception) {
//            e.printStackTrace()
//            null
//        }
//
//
//    }
    }
}