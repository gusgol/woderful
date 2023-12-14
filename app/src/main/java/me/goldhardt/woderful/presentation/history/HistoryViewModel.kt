package me.goldhardt.woderful.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import me.goldhardt.woderful.data.model.Workout
import me.goldhardt.woderful.domain.StreamWorkoutsUseCase
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    streamWorkoutsUseCase: StreamWorkoutsUseCase
) : ViewModel() {

    val workouts: Flow<PagingData<Workout>> = streamWorkoutsUseCase()
        .cachedIn(viewModelScope)
}