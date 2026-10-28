package com.daymark.app.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daymark.app.data.Activity
import com.daymark.app.data.ActivityRepository
import com.daymark.app.data.Entry
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DayUiModel(
    val date: LocalDate,
    val inMonth: Boolean,
    val colors: List<String>, // activity colors ticked on this date
)

data class CalendarUiState(
    val month: YearMonth = YearMonth.now(),
    val days: List<DayUiModel> = emptyList(),
    val activities: List<Activity> = emptyList(),
    val selectedDate: LocalDate? = null,
    val tickedByDate: Map<LocalDate, Set<Long>> = emptyMap(),
    val todayTickedIds: Set<Long> = emptySet(),
)

@OptIn(ExperimentalCoroutinesApi::class)
class CalendarViewModel(private val repo: ActivityRepository) : ViewModel() {

    private val month = MutableStateFlow(YearMonth.now())
    private val selectedDate = MutableStateFlow<LocalDate?>(null)

    private val today: LocalDate = LocalDate.now()

    val uiState: StateFlow<CalendarUiState> = combine(
        month,
        selectedDate,
        repo.activities(),
        month.flatMapLatest { m -> entriesInRange(m) },
        repo.entriesForDate(today.toString()),
    ) { m, selected, activities, entries, todayEntries ->
        val entriesByDate: Map<LocalDate, List<String>> = entries.groupBy(
            keySelector = { LocalDate.parse(it.date) },
            valueTransform = { entryColor(it, activities) },
        )
        CalendarUiState(
            month = m,
            days = buildDays(m, entriesByDate),
            activities = activities,
            selectedDate = selected,
            tickedByDate = entries.groupBy(
                keySelector = { LocalDate.parse(it.date) },
                valueTransform = { it.activityId },
            ).mapValues { it.value.toSet() },
            todayTickedIds = todayEntries.map { it.activityId }.toSet(),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CalendarUiState())

    private fun entriesInRange(m: YearMonth) =
        repo.entriesForDateRange(m.atDay(1).toString(), m.atEndOfMonth().toString())

    private fun entryColor(entry: Entry, activities: List<Activity>): String =
        activities.find { it.id == entry.activityId }?.color ?: "#888888"

    private fun buildDays(m: YearMonth, colorsByDate: Map<LocalDate, List<String>>): List<DayUiModel> {
        val firstDow = m.atDay(1).dayOfWeek.value % 7 // Sunday=0 (grid starts Sunday)
        val prevMonth = m.minusMonths(1)
        val days = mutableListOf<DayUiModel>()
        repeat(firstDow) { i ->
            val d = prevMonth.atEndOfMonth().minusDays((firstDow - 1 - i).toLong())
            days.add(DayUiModel(d, inMonth = false, colors = colorsByDate[d].orEmpty()))
        }
        for (d in 1..m.lengthOfMonth()) {
            val date = m.atDay(d)
            days.add(DayUiModel(date, inMonth = true, colors = colorsByDate[date].orEmpty()))
        }
        var next = m.plusMonths(1).atDay(1)
        while (days.size % 7 != 0) {
            days.add(DayUiModel(next, inMonth = false, colors = colorsByDate[next].orEmpty()))
            next = next.plusDays(1)
        }
        return days
    }

    fun tickedIdsFor(state: CalendarUiState, date: LocalDate): Set<Long> =
        state.tickedByDate[date].orEmpty()

    fun previousMonth() { month.value = month.value.minusMonths(1) }
    fun nextMonth() { month.value = month.value.plusMonths(1) }
    fun selectDate(date: LocalDate) { selectedDate.value = date }
    fun dismissPopup() { selectedDate.value = null }

    fun toggle(activityId: Long, date: LocalDate) {
        viewModelScope.launch { repo.toggleEntry(activityId, date.toString()) }
    }

    fun addActivity(name: String, color: String) {
        viewModelScope.launch { repo.addActivity(name, color) }
    }

    fun deleteActivity(activity: Activity) {
        viewModelScope.launch { repo.deleteActivity(activity) }
    }
}
