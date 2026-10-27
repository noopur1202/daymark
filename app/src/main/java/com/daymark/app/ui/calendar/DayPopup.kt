package com.daymark.app.ui.calendar

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.unit.dp
import com.daymark.app.data.Activity
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DayPopup(
    date: LocalDate,
    activities: List<Activity>,
    tickedIds: Set<Long>,
    onToggle: (Long) -> Unit,
    onDismiss: () -> Unit,
    onDelete: (Activity) -> Unit,
    onAdd: () -> Unit,
) {
    var pendingDelete by remember { mutableStateOf<Activity?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    date.format(DateTimeFormatter.ofPattern("EEE, d MMM yyyy")),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Check, contentDescription = "Done", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurface)
                }
            }
        },
        text = {
            Column {
                LazyColumn(modifier = Modifier.height(300.dp)) {
                    items(activities, key = { it.id }) { activity ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .combinedClickable(onClick = {}, onLongClick = { pendingDelete = activity }),
                        ) {
                            Box(Modifier.size(12.dp).background(Color(android.graphics.Color.parseColor(activity.color)), CircleShape))
                            Spacer(Modifier.size(12.dp))
                            Text(activity.name, modifier = Modifier.weight(1f))
                            Checkbox(checked = activity.id in tickedIds, onCheckedChange = { onToggle(activity.id) })
                        }
                    }
                }
                TextButton(onClick = onAdd) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.size(4.dp))
                    Text("Add activity", color = MaterialTheme.colorScheme.primary)
                }
            }
        },
        confirmButton = {},
        dismissButton = {},
    )

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
}
