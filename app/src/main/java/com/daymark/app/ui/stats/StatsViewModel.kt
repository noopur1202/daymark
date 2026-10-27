package com.daymark.app.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daymark.app.data.Activity
import com.daymark.app.data.ActivityRepository
import com.daymark.app.util.StatsFilter
import com.daymark.app.util.filterRange
import java.time.LocalDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

data class StatsRow(val activity: Activity, val count: Int)

data class StatsUiState(
    val filter: StatsFilter = StatsFilter.MONTH,
    val rows: List<StatsRow> = emptyList(),
)

@OptIn(ExperimentalCoroutinesApi::class)
class StatsViewModel(private val repo: ActivityRepository) : ViewModel() {

    private val filter = MutableStateFlow(StatsFilter.MONTH)
    private val today: LocalDate = LocalDate.now()

    private val counts = filter.flatMapLatest { f ->
        val (start, end) = filterRange(f, today)
        repo.activities().combine(repo.countsFlow(start.toString(), end.toString())) { acts, counts ->
            acts.map { StatsRow(it, counts[it.id] ?: 0) }
        }
    }

    val uiState: StateFlow<StatsUiState> = combine(filter, counts) { f, rows ->
        StatsUiState(filter = f, rows = rows.sortedByDescending { it.count })
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StatsUiState())

    fun setFilter(f: StatsFilter) { filter.value = f }
}
