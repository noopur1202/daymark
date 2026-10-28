package com.daymark.app.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FloatingActionButton
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
import com.daymark.app.data.Activity
import com.daymark.app.ui.theme.BlueAccent
import com.daymark.app.ui.theme.DarkBg
import com.daymark.app.ui.theme.OrangeAccent
import com.daymark.app.ui.theme.PinkAccent
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
                modifier = Modifier.weight(1f, fill = false).fillMaxWidth(),
            ) {
                items(state.days, key = { it.date.toString() }) { day ->
                    DayCell(
                        day = day,
                        isToday = day.date == LocalDate.now(),
                        isSelected = day.date == state.selectedDate,
                        onClick = { viewModel.selectDate(day.date) },
                    )
                }
            }
            ActivityPanel(
                activities = state.activities,
                tickedIds = state.panelTickedIds,
                listDate = state.panelDate,
                onToggle = { viewModel.toggle(it, state.panelDate) },
                onDelete = viewModel::deleteActivity,
                onColorChange = viewModel::changeColor,
            )
        }

        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = OrangeAccent,
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add activity", tint = DarkBg)
        }
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
        IconButton(onClick = onPrevious) { Text("<", color = PinkAccent, fontSize = 20.sp) }
        Text(
            "${month.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${month.year}",
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
        )
        IconButton(onClick = onNext) { Text(">", color = PinkAccent, fontSize = 20.sp) }
    }
}

@Composable
private fun DayCell(day: DayUiModel, isToday: Boolean, isSelected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(8.dp)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .background(color = MaterialTheme.colorScheme.surface, shape = shape)
            .then(
                if (isToday) Modifier.border(2.dp, BlueAccent, shape) else Modifier
            )
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
    ) {
        Text(
            day.date.dayOfMonth.toString(),
            fontSize = 13.sp,
            color = if (isToday) MaterialTheme.colorScheme.primary else if (day.inMonth) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
        )
        Spacer(Modifier.height(3.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            day.colors.take(3).forEach { c ->
                Box(Modifier.size(4.dp).height(10.dp).background(Color(android.graphics.Color.parseColor(c)), RoundedCornerShape(2.dp)))
            }
        }
    }
}

@Composable
private fun ColumnScope.ActivityPanel(
    activities: List<Activity>,
    tickedIds: Set<Long>,
    listDate: LocalDate,
    onToggle: (Long) -> Unit,
    onDelete: (Activity) -> Unit,
    onColorChange: (Activity, String) -> Unit,
) {
    var pendingDelete by remember { mutableStateOf<Activity?>(null) }
    var colorEdit by remember { mutableStateOf<Activity?>(null) }

    val today = LocalDate.now()
    if (listDate != today) {
        Text(
            listDate.format(java.time.format.DateTimeFormatter.ofPattern("EEE, d MMM")),
            color = MaterialTheme.colorScheme.primary,
            fontSize = 13.sp,
            modifier = Modifier.padding(vertical = 8.dp),
        )
    }
    if (activities.isEmpty()) {
        Box(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                "No activities recorded today",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                fontSize = 13.sp,
            )
        }
    } else {
        LazyColumn(contentPadding = PaddingValues(bottom = 88.dp)) {
            listItems(activities, key = { it.id }) { activity ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                ) {
                    Box(
                        Modifier
                            .size(12.dp)
                            .background(Color(android.graphics.Color.parseColor(activity.color)), CircleShape)
                            .clickable { colorEdit = activity },
                    )
                    Spacer(Modifier.size(12.dp))
                    Text(activity.name, modifier = Modifier.weight(1f))
                    Checkbox(
                        checked = activity.id in tickedIds,
                        onCheckedChange = { onToggle(activity.id) },
                    )
                    IconButton(onClick = { pendingDelete = activity }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete ${activity.name}",
                            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                        )
                    }
                }
            }
        }
    }

    pendingDelete?.let { activity ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Delete \"${activity.name}\"?") },
            text = { Text("This removes the activity and all days it was marked on.") },
            confirmButton = {
                TextButton(onClick = { onDelete(activity); pendingDelete = null }) {
                    Text("Delete", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("Cancel") }
            },
        )
    }

    colorEdit?.let { activity ->
        ChangeColorDialog(
            activity = activity,
            onConfirm = { color ->
                onColorChange(activity, color)
                colorEdit = null
            },
            onDismiss = { colorEdit = null },
        )
    }
}
