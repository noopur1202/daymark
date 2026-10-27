package com.daymark.app.util

import java.time.LocalDate

enum class StatsFilter(val label: String) {
    WEEK("Week"),
    MONTH("Month"),
    THREE_MONTHS("3M"),
    SIX_MONTHS("6M"),
    YEAR("Year"),
}

fun filterRange(filter: StatsFilter, today: LocalDate): Pair<LocalDate, LocalDate> =
    when (filter) {
        StatsFilter.WEEK -> today.minusDays(6) to today
        StatsFilter.MONTH -> today.withDayOfMonth(1) to today.withDayOfMonth(today.lengthOfMonth())
        StatsFilter.THREE_MONTHS -> today.minusMonths(2).withDayOfMonth(1) to today.withDayOfMonth(today.lengthOfMonth())
        StatsFilter.SIX_MONTHS -> today.minusMonths(5).withDayOfMonth(1) to today.withDayOfMonth(today.lengthOfMonth())
        StatsFilter.YEAR -> LocalDate.of(today.year, 1, 1) to LocalDate.of(today.year, 12, 31)
    }
