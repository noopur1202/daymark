package com.daymark.app.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class DateRangesTest {

    @Test
    fun `week is last 7 days including today`() {
        val (start, end) = filterRange(StatsFilter.WEEK, LocalDate.of(2026, 10, 27))
        assertEquals(LocalDate.of(2026, 10, 21), start)
        assertEquals(LocalDate.of(2026, 10, 27), end)
    }

    @Test
    fun `month is current calendar month`() {
        val (start, end) = filterRange(StatsFilter.MONTH, LocalDate.of(2026, 10, 27))
        assertEquals(LocalDate.of(2026, 10, 1), start)
        assertEquals(LocalDate.of(2026, 10, 31), end)
    }

    @Test
    fun `month handles february leap year`() {
        val (start, end) = filterRange(StatsFilter.MONTH, LocalDate.of(2024, 2, 10))
        assertEquals(LocalDate.of(2024, 2, 1), start)
        assertEquals(LocalDate.of(2024, 2, 29), end)
    }

    @Test
    fun `three months includes current month and two before`() {
        val (start, end) = filterRange(StatsFilter.THREE_MONTHS, LocalDate.of(2026, 10, 27))
        assertEquals(LocalDate.of(2026, 8, 1), start)
        assertEquals(LocalDate.of(2026, 10, 31), end)
    }

    @Test
    fun `six months handles year boundary`() {
        val (start, end) = filterRange(StatsFilter.SIX_MONTHS, LocalDate.of(2026, 2, 15))
        assertEquals(LocalDate.of(2025, 9, 1), start)
        assertEquals(LocalDate.of(2026, 2, 28), end)
    }

    @Test
    fun `year is calendar year`() {
        val (start, end) = filterRange(StatsFilter.YEAR, LocalDate.of(2026, 10, 27))
        assertEquals(LocalDate.of(2026, 1, 1), start)
        assertEquals(LocalDate.of(2026, 12, 31), end)
    }
}
