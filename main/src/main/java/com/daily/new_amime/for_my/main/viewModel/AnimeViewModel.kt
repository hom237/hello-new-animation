package com.daily.new_amime.for_my.main.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daily.new_amime.for_my.networking.anime.AnimeRepository
import com.daily.new_amime.for_my.networking.daily_anime.DailyDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject

@HiltViewModel
class AnimeViewModel @Inject constructor(
    private val animeRepository: AnimeRepository
) :  ViewModel() {

    private var _dailyAnimeUiState = MutableStateFlow<DailyAnimeUiState>(DailyAnimeUiState.Success(
        animes =  emptyList()))
    val dailyAnimeUiState: StateFlow<DailyAnimeUiState> = _dailyAnimeUiState

    fun getDailyAnime() = viewModelScope.launch(Dispatchers.IO) {
        kotlin.runCatching {
            animeRepository.getAnimationDaily()
        }.onSuccess { result ->
            result.onEach {
                _dailyAnimeUiState.emit(
                    DailyAnimeUiState.Success(
                        animes = it
                    )
                )
            }.launchIn(viewModelScope)
        }.onFailure { e ->
            if (e is HttpException) {
                if (e.code() == 500) {
                    _dailyAnimeUiState.emit(
                        DailyAnimeUiState.Error(exception = e, "서버에러")
                    )
                }
            }
        }
    }
}

sealed class DailyAnimeUiState {
    data class Success(val animes: List<DailyDto>) : DailyAnimeUiState()
    data class Error(val exception: Throwable, val m : String) : DailyAnimeUiState()
}