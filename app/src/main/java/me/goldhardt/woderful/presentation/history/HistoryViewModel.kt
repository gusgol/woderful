package me.goldhardt.woderful.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import me.goldhardt.woderful.data.Workout
import me.goldhardt.woderful.domain.StreamWorkoutsUseCase
import me.goldhardt.woderful.presentation.history.HistoryUiState.Success
import me.goldhardt.woderful.presentation.history.HistoryUiState.Error
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    streamWorkoutsUseCase: StreamWorkoutsUseCase
) : ViewModel() {

    val uiState: StateFlow<HistoryUiState> = streamWorkoutsUseCase()
        .map(::Success)
        .catch { Error(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HistoryUiState.Loading)
}

sealed interface HistoryUiState {
    object Loading : HistoryUiState
    data class Error(val throwable: Throwable) : HistoryUiState
    data class Success(val workouts: List<Workout>) : HistoryUiState
}