package com.daily.new_amime.for_my.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.daily.new_amime.for_my.main.ui.theme.Daily_anime_appTheme
import com.daily.new_amime.for_my.main.viewModel.AnimeViewModel
import com.daily.new_amime.for_my.main.viewModel.DailyAnimeUiState
import com.daily.new_amime.for_my.networking.anime.AnimeRepository
import com.daily.new_amime.for_my.networking.image.ImageRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var animeRepository: AnimeRepository

    @Inject
    lateinit var imageRepository: ImageRepository
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val animeViewModel : AnimeViewModel by viewModels()
            Daily_anime_appTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding),
                        animeViewModel
                    )
                }
            }
        }
    }
}


@Composable
fun Greeting(name: String, modifier: Modifier = Modifier, viewModel: AnimeViewModel) {
    val dailyAnimeUiState by viewModel.dailyAnimeUiState.collectAsState()
    Text(
        text = "Hello $name!",
        modifier = modifier.clickable {
            viewModel.getDailyAnime()
        }
    )
    Text(text = when (dailyAnimeUiState){
        is DailyAnimeUiState.Success ->{
            (dailyAnimeUiState as DailyAnimeUiState.Success).animes.toString()
        }
        is DailyAnimeUiState.Error ->{
            (dailyAnimeUiState as DailyAnimeUiState.Error).m
        }
    }
    )
}

//@Preview(showBackground = true)
//@Composable
//fun GreetingPreview() {
//    Daily_anime_appTheme {
//        Greeting("Android")
//    }
//}