package com.daymark.app.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items as listItems
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

private val weekdayNames = listOf("S", "M", "T", "W", "T", "F", "S")

@Composable
fun CalendarScreen(viewModel: CalendarViewModel, modifier: Modifier = Modifier) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            MonthHeader(
                month = state.month,
                onPrevious = viewModel::previousMonth,
                onNext = viewModel::nextMonth,
            )
            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                weekdayNames.forEach { Text(it, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f), fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.weight(1f)) }
            }
            Spacer(Modifier.height(6.dp))
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.weight(1f).fillMaxWidth(),
            ) {
                items(state.days, key = { it.date.toString() }) { day ->
                    DayCell(
                        day = day,
                        isToday = day.date == LocalDate.now(),
                        onClick = { viewModel.selectDate(day.date) },
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.15f))
            TodayPanel(
                activities = state.activities,
                tickedIds = state.todayTickedIds,
                onToggle = { viewModel.toggle(it, LocalDate.now()) },
                onAdd = { showAddDialog = true },
            )
        }

        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add activity", tint = Color.White)
        }
    }

    state.selectedDate?.let { date ->
        DayPopup(
            date = date,
            activities = state.activities,
            tickedIds = viewModel.tickedIdsFor(state, date),
            onToggle = { viewModel.toggle(it, date) },
            onDismiss = viewModel::dismissPopup,
            onDelete = viewModel::deleteActivity,
            onAdd = { showAddDialog = true },
        )
    }

    if (showAddDialog) {
        AddActivityDialog(
            onConfirm = { name, color ->
                viewModel.addActivity(name, color)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false },
        )
    }
}

@Composable
private fun MonthHeader(month: YearMonth, onPrevious: () -> Unit, onNext: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        IconButton(onClick = onPrevious) { Text("<", color = MaterialTheme.colorScheme.primary, fontSize = 20.sp) }
        Text(
            "${month.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${month.year}",
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
        )
        IconButton(onClick = onNext) { Text(">", color = MaterialTheme.colorScheme.primary, fontSize = 20.sp) }
    }
}

@Composable
private fun TodayPanel(
    activities: List<com.daymark.app.data.Activity>,
    tickedIds: Set<Long>,
    onToggle: (Long) -> Unit,
    onAdd: () -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Text(
            "Today",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier.weight(1f),
        )
        TextButton(onClick = onAdd) {
            Text("+ Add", color = MaterialTheme.colorScheme.primary)
        }
    }
    if (activities.isEmpty()) {
        Text(
            "No activities recorded today",
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
            fontSize = 13.sp,
            modifier = Modifier.padding(vertical = 8.dp),
        )
    } else {
        LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
            listItems(activities, key = { it.id }) { activity ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                ) {
                    Box(Modifier.size(12.dp).background(Color(android.graphics.Color.parseColor(activity.color)), CircleShape))
                    Spacer(Modifier.size(12.dp))
                    Text(activity.name, modifier = Modifier.weight(1f))
                    Checkbox(
                        checked = activity.id in tickedIds,
                        onCheckedChange = { onToggle(activity.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun DayCell(day: DayUiModel, isToday: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .background(
                color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(8.dp),
            )
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
    ) {
        Text(
            day.date.dayOfMonth.toString(),
            fontSize = 13.sp,
            color = if (isToday) Color.White else if (day.inMonth) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
        )
        Spacer(Modifier.height(3.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            day.colors.take(3).forEach { c ->
                Box(Modifier.size(4.dp).height(10.dp).background(Color(android.graphics.Color.parseColor(c)), RoundedCornerShape(2.dp)))
            }
        }
    }
}
