package com.daily.new_amime.for_my.main.ui.wiget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.glance.Button
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.Text
import androidx.lifecycle.viewModelScope
import com.daily.new_amime.for_my.main.viewModel.DailyAnimeUiState
import com.daily.new_amime.for_my.networking.anime.AnimeApi
import com.daily.new_amime.for_my.networking.anime.AnimeRepository
import com.daily.new_amime.for_my.networking.daily_anime.DailyDto
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject


class NewAnimeWidget(private val data :  suspend () -> Flow<List<DailyDto>>)
    : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        GlobalScope.launch() {
            Log.d("test", "input data : ${data()}")
        }
        provideContent {

            // create your AppWidget here
            GlanceTheme {
                MyContent()
            }
        }
    }


    @Composable
    private fun MyContent() {
        Log.d("size", "widget size : $sizeMode")
        val cont = remember {
            mutableStateOf(0)
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

            Text(text = "count : ${cont.value}", modifier = GlanceModifier.padding(12.dp))
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